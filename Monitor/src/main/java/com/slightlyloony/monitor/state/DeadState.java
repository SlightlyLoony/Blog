package com.slightlyloony.monitor.state;

import com.slightlyloony.common.ipmsgs.IPMsg;
import com.slightlyloony.common.ipmsgs.IPMsgSocket;
import com.slightlyloony.common.ipmsgs.IPMsgType;
import com.slightlyloony.monitor.MailPortal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DeadState extends MonitoredProcessState {

    private static Logger LOG = LogManager.getLogger();


    private boolean gotAliveMessage;
    private boolean gotWebAliveMessage;
    private ScheduledFuture isAliveCheckFuture;
    private ScheduledFuture isWebAliveCheckFuture;


    protected DeadState( final MonitoredServerStateMachine _parent ) {
        super( _parent, null );
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

            case INITIALIZE:         handleInitialize();      break;
            case IS_ALIVE_CHECK:     handleIsAliveCheck();    break;
            case ALIVE:              handleAlive();           break;
            case WEB_ALIVE:          handleWebAlive();        break;
            case IS_WEB_ALIVE_CHECK: handleIsWebAliveCheck(); break;
            default:                 return false;
        }
        return true;
    }


    private void handleInitialize() {

        try {

            // initialize our state...
            gotAliveMessage = false;
            gotWebAliveMessage = false;

            // get our process started...
            LOG.info( msg( "Starting process in {0}", parent.participant ) );
            getProcess().runProcess();

            // check back in a few seconds to make sure it actually DID start...
            isAliveCheckFuture = delayed( 10, TimeUnit.SECONDS, Event.IS_ALIVE_CHECK );
        }
        catch( IOException e ) {
            LOG.error( msg( "Error when attempting to run process in {0}", parent.participant ), e );
            parent.transitionTo( new ErrorState( parent ) );
        }
    }


    private void handleIsAliveCheck() {

        if( getProcess().isAlive() ) {
            LOG.info( msg( "Process in {0} is alive", parent.toString() ) );
            if( gotAliveMessage ) {
                LOG.info( msg( "IsAlive message received for {0} server", parent.participant ) );

                // send message to start web server...
                try {
                    IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.StartWeb, null ), parent.participant );
                }
                catch( IOException e ) {
                    LOG.error( "Problem sending start web server message" );
                }

                // check back in a few seconds to make sure it actually DID start...
                isWebAliveCheckFuture = delayed( 10, TimeUnit.SECONDS, Event.IS_WEB_ALIVE_CHECK );
            }
            else {
                LOG.error( msg( "Process in {0} did not send alive message after starting", parent.participant ) );
                parent.transitionTo( new ErrorState( parent ) );
            }
        }
        else {
            LOG.error( msg( "Process in {0} did not come alive after starting", parent.participant ) );
            parent.transitionTo( new ErrorState( parent ) );
        }
    }


    private void handleAlive() {

        // make sure we know the process is alive...
        gotAliveMessage = true;
        handleIsAliveCheck();

        // kill the alive check if it hasn't already run...
        isAliveCheckFuture.cancel( true );
    }


    private void handleWebAlive() {

        // make sure we know the web server is alive...
        gotWebAliveMessage = true;
        handleIsWebAliveCheck();

        // kill the alive check if it hasn't already run...
        isWebAliveCheckFuture.cancel( true );
    }


    private void handleIsWebAliveCheck() {

        if( gotWebAliveMessage ) {
            LOG.info( msg( "Web server in {0} is running", parent.participant ) );
            parent.transitionTo( new AliveState( parent ) );
            MailPortal.sendMessage( parent.participant + " started", "Thought you might like to know..." );
        }
        else {
            LOG.error( msg( "Web server in {0} did not come alive after starting", parent.participant ) );
            parent.transitionTo( new ErrorState( parent ) );
        }

    }
}
