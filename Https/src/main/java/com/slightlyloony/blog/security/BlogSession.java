package com.slightlyloony.blog.security;

import com.slightlyloony.blog.users.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import static com.slightlyloony.blog.security.BlogSession.BlogSessionState.*;
import static com.slightlyloony.common.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogSession {

    // pre-determined names for commonly used session objects (the asterisks are there to make collisions unlikely)...
    private final static String USER      = "user**************";
    private final static String CREATED   = "created***********";
    private final static String ENTRYPAGE = "entrypage*********";
    private final static String LASTPAGE  = "lastpage**********";

    private final static Logger LOG = LogManager.getLogger();

    private final String token;

    private String name;  // user name, or dotted IP address if anonymous...

    // these values MUST be accessed ONLY in synchronized methods or the constructor...
    private long lastUsed;
    private BlogSessionState state;

    // where the actual session data is held...
    private final ConcurrentHashMap<String,Object> sessionData;


    public BlogSession( final String _token ) {
        token = _token;
        state = BlogSessionState.ACTIVE;
        lastUsed = System.currentTimeMillis();
        sessionData = new ConcurrentHashMap<>();
        sessionData.put( CREATED, Instant.now() );
    }


    public String getName() {
        return name;
    }


    public void setName( final String _name ) {
        name = _name;
    }


    public String getToken() {
        return token;
    }


    public Object get( final String _name ) {
        return sessionData.get( _name );
    }


    public void put( final String _name, final Object _info ) {
        sessionData.put( _name, _info );
    }


    public User getUser() {
        return (User) sessionData.get( USER );
    }


    public Instant getCreationTimestamp() {
        return (Instant) sessionData.get( CREATED );
    }


    public void setEntryPage( final String _entryPage ) {
        sessionData.put( ENTRYPAGE, _entryPage );
    }


    public void setLastPage( final String _lastPage ) {
        sessionData.put( LASTPAGE, _lastPage );
    }


    public String getEntryPage() {
        return (String) sessionData.get( ENTRYPAGE );
    }


    public String getLastPage() {
        return (String) sessionData.get( LASTPAGE );
    }


    public void putUser( final User _user ) {
        sessionData.put( USER, _user );
    }


    /**
     * Handles the details of managing the lifecycle of a blog session.  There are two critical times, both based on how long it's been since
     * this session was last used.  The first critical time is when to inactivate the session, which frees up its memory and requires the user to
     * establish a new session the next time he browses to the blog web site.  The second critical time is when to actually remove the session from
     * the collection of sessions.  Until it's actually removed, its presence prevents another session with the same token from being created.  This
     * costs us a bit of memory, but reduces the probability of an accidentally duplicated session token (which could then be accessed by two users)
     * to a very low level (we hope!).
     *
     * @param _inactivationThreshold if this session was last used earlier than this, then inactivate the session
     * @param _removalThreshold if this session was last used earlier than this, then kill (remove) the session
     * @return the (possibly new) state of this session
     */
    public synchronized BlogSessionState manageLifecycle( final long _inactivationThreshold, final long _removalThreshold ) {

        // if we're already dead, just leave...
        if( state == DEAD )
            return state;

        // if we're active, check if it's time to inactivate...
        if( state == ACTIVE ) {

            // if it's not time to inactivate, just leave...
            if( lastUsed > _inactivationThreshold )
                return state;

            // otherwise it's time to inactivate...
            sessionData.clear();  // free up any memory used...
            LOG.info( msg( "Inactivating session {0} for {1}", token, name ) );
            state = INACTIVE;
            return state;
        }

        // if we get here, the state is INACTIVE, so see if it's time to die...
        if( lastUsed <= _removalThreshold ) {
            LOG.info( msg( "Killing inactive session {0} for {1}", token, name ) );
            state = DEAD;
        }
        return state;
    }


    /**
     * Invoked to stake a claim to use of this blog session.  If successful, returns with ACTIVE state.  Otherwise, returns with either INACTIVE or
     * DEAD state, and must not be used by the caller.
     *
     * @return the state of the blog session after making a claim.
     */
    public synchronized BlogSessionState claim() {

        if( state != ACTIVE )
            return state;

        lastUsed = System.currentTimeMillis();
        return state;
    }


    public enum BlogSessionState {

        ACTIVE,
        INACTIVE,
        DEAD;
    }
}
