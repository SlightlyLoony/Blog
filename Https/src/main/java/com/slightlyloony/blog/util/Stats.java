package com.slightlyloony.blog.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slightlyloony.blog.events.Event;
import com.slightlyloony.blog.events.EventListener;
import com.slightlyloony.blog.events.Events;
import com.slightlyloony.blog.security.BlogSession;
import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Map;

import static com.slightlyloony.blog.events.EventType.*;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Stats implements EventListener {

    private static final Logger LOG = LogManager.getLogger();

    private static final Stats INSTANCE = new Stats();


    private final Map<String,UserRecord> users;
    private int userLogins;
    private int userLoginFailures;
    private int pages;
    private int uncachedHits;
    private int cacheHits;
    private int cacheMisses;
    private long cacheBytesRead;
    private long diskBytesRead;


    @Override
    public synchronized void onEvent( final Event _event ) {

        BlogSession session;
        UserRecord record;

        switch( _event.getType() ) {

            case USER_LOGIN:
                userLogins++;
                ensureUser( (BlogSession) _event.getParam( 0 ) );
                break;

            case USER_LOGIN_FAILURE:
                userLoginFailures++;
                break;

            case PAGE_HIT:
                session = (BlogSession) _event.getParam( 0 );
                pages++;
                if( session.getUser() != null ) {
                    record = ensureUser( session );
                    record.pages++;
                    record.exitPage = session.getLastPage();
                    record.exitTime = Instant.now();
                }
                break;

            case SESSION_KILLED:
                session = (BlogSession) _event.getParam( 0 );
                if( !session.getUser().getUsername().startsWith( "anonymous***" )) {
                    record = users.get( session.getUser().getUsername() );
                    if( record != null ) {
                        users.remove( session.getUser().getUsername() );
                        LOG.info( LU.msg( "User session ended for {0}: entry {1}, exit {2}, from {3} to {4}, visiting {5} pages",
                                record.username, record.entryPage, record.exitPage,
                                record.entryTime.toString(), record.exitTime.toString(), record.pages ) );
                    }
                }
                break;

            case CACHE_HIT:
                cacheHits++;
                cacheBytesRead += (Integer) _event.getParam( 1 );
                break;

            case CACHE_MISS:
                cacheMisses++;
                diskBytesRead += (Integer) _event.getParam( 1 );
                break;

            case UNCACHED_READ:
                uncachedHits++;
                diskBytesRead += (Integer) _event.getParam( 1 );
                break;
        }
    }


    /**
     * Returns a JSON-formatted statistical report.
     *
     * @return the JSON-formatted statistical report.
     */
    private synchronized String reportImpl() {
        Gson gson = new Gson();
        JsonObject object = new JsonObject();

        JsonArray userArray = new JsonArray();
        for( UserRecord record : users.values() ) {
            JsonObject user = new JsonObject();
            user.addProperty( "username",  record.username             );
            user.addProperty( "entryPage", record.entryPage            );
            user.addProperty( "exitPage",  record.exitPage             );
            user.addProperty( "entryTime", record.entryTime.toString() );
            user.addProperty( "exitTime",  record.exitTime.toString()  );
            user.addProperty( "pages",     record.pages                );
            userArray.add( user );
        }
        object.add( "users", userArray );
        object.addProperty( "pages",             pages             );
        object.addProperty( "userLogins",        userLogins        );
        object.addProperty( "userLoginFailures", userLoginFailures );
        object.addProperty( "cacheHits",         cacheHits         );
        object.addProperty( "cacheMisses",       cacheMisses       );
        object.addProperty( "uncachedHits",      uncachedHits      );
        object.addProperty( "cacheBytesRead",    cacheBytesRead    );
        object.addProperty( "diskBytesRead",     diskBytesRead     );
        object.addProperty( "loggedInUsers",     users.size()      );

        return gson.toJson( object );
    }


    public static void init() {
        Events.registerListener( INSTANCE, USER_LOGIN, USER_LOGIN_FAILURE, PAGE_HIT, SESSION_KILLED, UNCACHED_READ, CACHE_HIT, CACHE_MISS );
    }


    public static String report() {
        return INSTANCE.reportImpl();
    }


    private Stats() {
        users = Maps.newHashMap();
    }


    private UserRecord ensureUser( final BlogSession _session ) {

        UserRecord result = users.get( _session.getUser().getUsername() );
        if( result == null ) {
            result = new UserRecord();
            result.username = _session.getUser().getUsername();
            result.entryTime = _session.getCreationTimestamp();
            result.exitTime = _session.getCreationTimestamp();
            result.pages = 1;
            result.entryPage = _session.getEntryPage();
            result.exitPage = _session.getLastPage();
            users.put( _session.getUser().getUsername(), result );
        }
        return result;
    }


    private static class UserRecord {
        private String username;
        private int pages;
        private Instant entryTime;
        private Instant exitTime;
        private String entryPage;
        private String exitPage;
    }
}
