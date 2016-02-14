package com.slightlyloony.blog.events;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implements a simple asynchronous event handling system.  Each event may have 0..n parameters of any type, and these parameters are type-checked.
 * They are asynchronous in the sense that the thread firing the event does not actually <i>process</i> the event; the processing is handled in a
 * separate thread.  There is a single event processing thread, which processes the events from a queue in the order in which they were received.
 * Events are &ldquo;one-way&rdquo; &ndash; the event firing the thread receives no feedback about how (or if) the event is processed.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Events {

    private static final Events INSTANCE = new Events();

    private static final int MAX_QUEUE_SIZE = 1000;
    private static final Logger LOG = LogManager.getLogger();

    private final SetMultimap<EventType,EventListener> listeners;
    private final LinkedBlockingQueue<Event> eventQueue;
    private final Thread eventProcessor;


    private Events() {
        eventQueue = new LinkedBlockingQueue<>( MAX_QUEUE_SIZE );
        listeners = HashMultimap.create();

        // start our processing thread...
        eventProcessor = new EventProcessor();
        eventProcessor.start();
    }


    public static void registerListener( final EventListener _eventListener, final EventType... _eventTypes ) {

        if( (_eventListener == null) || (_eventTypes == null) || (_eventTypes.length == 0))
            throw new HandlerIllegalArgumentException( "Missing EventListener or EventType arguments" );

        INSTANCE.registerListenerImpl( _eventListener, _eventTypes );
    }


    public static void fire( final Event _event ) {

        if( _event == null )
            throw new HandlerIllegalArgumentException( "Event is missing" );

        INSTANCE.fireImpl( _event );
    }


    private synchronized void fireImpl( final Event _event ) {
        eventQueue.add( _event );
    }


    private synchronized void registerListenerImpl( final EventListener _eventListener, final EventType... _eventTypes ) {
        for( EventType eventType : _eventTypes )
            listeners.put( eventType, _eventListener );
    }


    private class EventProcessor extends Thread {


        private EventProcessor() {
            setDaemon( true );
            setName( "EventProcessor" );
        }

        public void run() {

            while( !interrupted() ) {

                try {

                    // wait patiently until we get an event...
                    Event event = eventQueue.take();

                    // invoke all the listeners on it...
                    for( EventListener eventListener : listeners.get( event.getType() ) )
                        eventListener.onEvent( event );
                }
                catch( final Throwable t ) {
                    LOG.warn( "Uncaught exception in event processor: " + t.getMessage(), t );
                }
            }
        }
    }
}
