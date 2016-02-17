package com.slightlyloony.blog.security;

import com.google.common.io.BaseEncoding;
import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.events.EventType;
import com.slightlyloony.blog.events.Events;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.util.Timer;
import com.slightlyloony.common.ExecutionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * Implements a browser session manager for the blog, independently from Jetty's built-in session management.  We implemented Jetty's session
 * management at first, but were unhappy with its complexity and opaqueness, as well as with its performance.  It's designed for a far more complex
 * and varied environment than we need for the blog.  Simpler, more transparent, and more performant is what we want ... so we rolled our own.
 * <p>
 * This is a classic session manager that works by using session cookies.  Session cookies are issued with randomly generated tokens, and a simple
 * map keeps track of these.  The tokens timeout after a configurable idle period, and timed-out tokens are tracked for 10x the idle period to
 * ensure they are not accidentally reused.
 * <p>
 * This class is a singleton.  It is threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogSessionManager {

    public final static BlogSessionManager INSTANCE = new BlogSessionManager();

    private final static Logger LOG = LogManager.getLogger();
    private final static int TOKEN_FILL_INTERVAL_MS = 1000;
    private final static int SESSION_SCAVENGE_INTERVAL = 30;  // in multiples of TOKEN_FILL_INTERVAL...
    private final static int TOKEN_QUEUE_SIZE = 250;
    private final static int REMOVAL_MULTIPLE = 10; // how long to wait before removing a session, in idle timeout intervals...

    private final ConcurrentHashMap<String,BlogSession> sessions;
    private final SecureRandom random;
    private final LinkedBlockingQueue<String> newTokens;
    private final long idleTimeout;

    private int tokenFillIntervals;


    private BlogSessionManager() {

        // set up our basic structures...
        sessions = new ConcurrentHashMap<>();
        newTokens = new LinkedBlockingQueue<>( TOKEN_QUEUE_SIZE );
        idleTimeout = ServerInit.getConfig().getSessionIdleTimeout();

        tokenFillIntervals = 0;

        // make our random number generator using advice from https://www.cigital.com/blog/proper-use-of-javas-securerandom/
        try {

            // just some obfuscation, by varying the size of the initial "get" (50..177 bytes)...
            byte[] seed = new byte[50 + (int)(System.currentTimeMillis() & 0x7f)];

            // ensure we have the same provider on any platform...
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");

            // forces internal secure seeding; may block briefly...
            random.nextBytes( seed );
        }
        catch( NoSuchAlgorithmException | NoSuchProviderException e ) {
            LOG.fatal( "Problem instantiating SecureRandom", e );
            throw new HandlerIllegalStateException( "No session token generator" );
        }
    }


    /**
     * Returns the blog session with the given session ID if it exists and is active; otherwise return null.  If this call successfully returns a
     * blog session, the last-used time of the associated token is updated to the current time.
     *
     * @param _sessionID the ID of the desired session
     * @return the desired session, or null if that session is not available
     */
    public BlogSession claimSession( final String _sessionID ) {

        BlogSession session = sessions.get( _sessionID );
        if( session == null )
            return null;

        return (session.claim() == BlogSession.BlogSessionState.ACTIVE) ? session : null;
    }


    /**
     * Creates a new blog session and returns it.
     *
     * @return the newly created blog session
     */
    public BlogSession create() {

        try {
            // get a token that hasn't been used yet...
            String token = null;
            while( token == null ) {
                token = newTokens.take();
                if( sessions.containsKey( token ) )
                    token = null;
            }

            // we got one, so now make our shiny new blog session and add it to our collection...
            BlogSession session = new BlogSession( token );
            sessions.put( token, session );
            return session;
        }
        catch( InterruptedException e ) {
            LOG.error( "Interrupted while waiting for a token", e );
            return null;
        }
    }


    public void init() {

        // set up our periodic maintenance job...
        ExecutionService.INSTANCE.scheduleAtFixedRate( BlogSessionManager.INSTANCE::maintain, 0, TOKEN_FILL_INTERVAL_MS, TimeUnit.MILLISECONDS );
    }


    private void maintain() {

        Timer t = new Timer();

        // if we don't have enough tokens in inventory, add them...
        int added = 0;
        while( newTokens.remainingCapacity() > 0 ) {

            // obtain a nice, shiny new token...
            String token = generateToken();

            // if we already have this new token in our sessions, skip it and get another...
            // note that this DOESN'T check to see if we already have this token in our queue...
            if( sessions.containsKey( token ))
                continue;

            // try to add our shiny new token, but if we fail because the queue is full, just bail out...
            if( !newTokens.offer( token ) )
                break;

            added++;
        }

        t.mark();

        // if it's not time to scavenge, then we're done...
        tokenFillIntervals++;
        if( tokenFillIntervals < SESSION_SCAVENGE_INTERVAL ) {
            if( added > 0 )
                LOG.info( msg( "Added {1} new session tokens in {0}", t.toString(), added ) );
            return;
        }

        tokenFillIntervals = 0;

        // see if any of our existing sessions have timed out...
        long inactivateThreshold = System.currentTimeMillis() - (1000 * idleTimeout);
        long removalThreshold = System.currentTimeMillis() - (REMOVAL_MULTIPLE * 1000 * idleTimeout);
        Iterator<BlogSession> it = sessions.values().iterator();
        int scavenged = 0;
        while( it.hasNext() ) {

            BlogSession session = it.next();
            if( BlogSession.BlogSessionState.DEAD == session.manageLifecycle( inactivateThreshold, removalThreshold ) ) {
                Events.fire( EventType.SESSION_KILLED, session );
                it.remove();
                scavenged++;
            }
        }

        t.mark();
        if( added > 0 )
            LOG.info( msg( "Added {2} new session tokens in {0}, scavenged {3} dead sessions in {1}",
                    t.toString( 1 ), t.toString( 1, 2 ), added, scavenged ) );
        else
            LOG.info( msg( "Scavenged {1} dead sessions in {0}", t.toString( 1, 2 ), scavenged ) );
    }


    private String generateToken() {

        // use our random generator to make up a new token - 15 bytes will get us 20 base64 characters...
        byte[] bytes = new byte[24];
        random.nextBytes( bytes );
        BaseEncoding encoder = BaseEncoding.base64Url();
        return encoder.encode( bytes );
    }
}
