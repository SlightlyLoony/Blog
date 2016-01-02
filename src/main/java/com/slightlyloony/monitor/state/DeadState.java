package com.slightlyloony.monitor.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.slightlyloony.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DeadState extends MonitoredProcessState {

    private static Logger LOG = LogManager.getLogger();


    protected DeadState( final MonitoredServerStateMachine _parent ) {
        super( _parent, getChildStateMachine() );
    }


    private static StateMachine getChildStateMachine() {
        return null;
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

        switch( _event ) {

            case INITIALIZE:     handleInitialize();   break;
            case IS_ALIVE_CHECK: handleIsAliveCheck(); break;

            default: return false;

        }
        return true;
    }


    private void handleInitialize() {

        try {

            // get our process started...
            LOG.info( msg( "Starting process in {0}", parent.toString() ) );
            getProcess().runProcess();

            // check back in a few seconds to make sure it actually DID start...
            delayed( 5, TimeUnit.SECONDS, Event.IS_ALIVE_CHECK );
        }
        catch( IOException e ) {
            LOG.error( msg( "Error when attempting to run process in {0}", parent.toString() ), e );
            parent.transitionTo( new ErrorState( parent ) );
        }
    }


    private void handleIsAliveCheck() {

        if( getProcess().isAlive() ) {
            LOG.info( msg( "Process in {0} is alive", parent.toString() ) );
        }
        else {
            LOG.error( msg( "Process in {0} did not come alive after starting", parent.toString() ) );
            parent.transitionTo( new ErrorState( parent ) );
        }
    }
}
