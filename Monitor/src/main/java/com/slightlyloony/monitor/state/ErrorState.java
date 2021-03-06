package com.slightlyloony.monitor.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ErrorState extends MonitoredProcessState {

    private static Logger LOG = LogManager.getLogger();


    protected ErrorState( final MonitoredServerStateMachine _parent ) {
        super( _parent, null );

        // tell our process to shutdown, and log what happened...
        LOG.error( msg( "Fatal error in {0}; shutting process down permanently", parent.participant ) );
        parent.process.stopProcess();
    }


    /**
     * The parent state machine calls this method to handle the given event (with the given optional data).  If this state can handle the event,
     * do so and return true (to indicate that the event was handled).  Otherwise, do nothing and return false.
     *
     * @param _event the event to be handled.
     * @param _data any data associated with the event.
     * @return true if the event was handled.
     */
    @Override
    protected boolean handleEvent( final Event _event, final Object... _data ) {
        return false;
    }


    private void handleInitialize() {

        // tell our process to shutdown (and wait for that to complete)...
        LOG.info( msg( "Stopping process in {0}", parent.toString() ) );
        parent.process.stopProcess();

        // now let's go back to dead state and reinitialize...
        LOG.info( msg( "Starting process in {0}", parent.toString() ) );
        parent.transitionTo( new DeadState( parent ) );
        parent.on( Event.INITIALIZE );
    }
}
