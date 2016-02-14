package com.slightlyloony.blog.util;

import com.slightlyloony.blog.events.Event;
import com.slightlyloony.blog.events.EventListener;
import com.slightlyloony.blog.events.EventType;
import com.slightlyloony.blog.events.Events;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Stats implements EventListener {

    private static final Logger LOG = LogManager.getLogger();

    private static final Stats INSTANCE = new Stats();


    @Override
    public void onEvent( final Event _event ) {
        LOG.info( "Got event: " + _event.getType() + ", " + _event.getParam( 0 ) );
    }


    public static void init() {
        Events.registerListener( INSTANCE, EventType.USER_LOGIN, EventType.USER_LOGIN_FAILURE );
    }


    private Stats() {
        // prevent instantiation...
    }
}
