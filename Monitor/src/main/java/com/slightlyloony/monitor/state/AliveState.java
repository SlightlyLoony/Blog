package com.slightlyloony.monitor.state;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.common.ipmsgs.IPMsgParticipant;
import com.slightlyloony.monitor.MonitorConfig;
import com.slightlyloony.monitor.MonitorInit;
import com.slightlyloony.monitor.TestURL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class AliveState extends MonitoredProcessState {

    private static Logger LOG = LogManager.getLogger();


    private ScheduledFuture testerFuture;
    private Runnable testRunner;
    private int consecutiveFailures;

    protected AliveState( final MonitoredServerStateMachine _parent ) {
        super( _parent, null );
        consecutiveFailures = 0;

        // now run the test the first time, 5 seconds after starting our web server...
        testRunner = getTestRunner();
        testerFuture = ExecutionService.INSTANCE.schedule( testRunner, 5, TimeUnit.SECONDS );
    }


    /**
     * The parent state machine calls this method to handle the given event (with the given optional data).  If this state can handle the event, do so
     * and return true (to indicate that the event was handled).  Otherwise, do nothing and return false.
     *
     * @param _event the event to be handled.
     * @param _data  any data associated with the event.
     * @return true if the event was handled.
     */
    @Override
    protected boolean handleEvent( final Event _event, final Object... _data ) {

        switch( _event ) {
            case WEB_TEST_SUCCESS:   handleTestSuccess(); break;
            case WEB_TEST_FAILURE:   handleTestFailure(); break;
            case RESTART:            handleRestart();     break;
            default:                 return false;
        }
        return true;
    }


    private void handleTestSuccess() {
        consecutiveFailures = 0;
        LOG.info( msg( "Test succeeded for {0}", parent.participant ) );
    }


    private void handleTestFailure() {
        consecutiveFailures++;
        LOG.info( msg( "Test failed for {0}", parent.participant ) );

        // if we'ev failed less than three times consecutively, then we're not ready to take any action yet...
        if( consecutiveFailures < 3 )
            return;

        // three or more consecutive failures means it's time to restart the server...
        LOG.warn( msg( "Test for {0} failed for three or more consecutive attempts; restarting server", parent.participant ) );
        handleRestart();
    }


    private void handleRestart() {
        LOG.info( msg( "Restart requested for {0}", parent.participant ) );
        parent.transitionTo( new RestartingState( parent ) );
        parent.on( Event.INITIALIZE );
    }


    @Override
    public void from() {

        // if we still have a tester scheduled, cancel it...
        if( testerFuture != null )
            testerFuture.cancel( true );
    }


    private Runnable getTestRunner() {

        // set up our test runner...
        MonitorConfig.Server config = (parent.participant
                == IPMsgParticipant.HTTP) ? MonitorInit.getConfig().getHttp() : MonitorInit.getConfig().getHttps();

        return () -> {

            // run our test in another thread, so we don't hold up the executor thread...
            final Thread testerThread = new Thread( () -> {

                // try to read from our test URL...
                TestURL tester = new TestURL( config.getTest() );
                boolean testGood = tester.test();

                // if we were successful, send a SUCCESS event and reschedule for 15 seconds later...
                if( testGood ) {
                    parent.on( Event.WEB_TEST_SUCCESS );
                    testerFuture = ExecutionService.INSTANCE.schedule( testRunner, 15, TimeUnit.SECONDS );
                }

                // if we failed, send a FAILURE event and reschedule for 5 seconds later...
                else {
                    parent.on( Event.WEB_TEST_FAILURE );
                    testerFuture = ExecutionService.INSTANCE.schedule( testRunner, 5, TimeUnit.SECONDS );
                }

            });
            testerThread.setName( parent.participant + "-tester" );
            testerThread.setDaemon( true );
            testerThread.start();
        };
    }
}
