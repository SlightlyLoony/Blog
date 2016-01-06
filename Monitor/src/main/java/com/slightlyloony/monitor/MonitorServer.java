package com.slightlyloony.monitor;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.common.ipmsgs.IPMsg;
import com.slightlyloony.common.ipmsgs.IPMsgParticipant;
import com.slightlyloony.common.ipmsgs.IPMsgSocket;
import com.slightlyloony.common.ipmsgs.IPMsgType;
import com.slightlyloony.monitor.state.Event;
import com.slightlyloony.monitor.state.MonitoredServerStateMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitorServer {


    private static Logger LOG;


    private static MonitoredServerStateMachine HTTP = null;
    private static MonitoredServerStateMachine HTTPS = null;
    private static boolean SHUTDOWN = false;


    public static void main( final String[] _args ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        LOG = LogManager.getLogger();

        // initialization of the monitor application...
        MonitorInit.init();

        // set up our monitored processes...
        MonitoredProcess http = new MonitoredProcess( IPMsgParticipant.HTTP, MonitorInit.getConfig().getHttp() );
        HTTP = new MonitoredServerStateMachine( IPMsgParticipant.HTTP, http );

//        MonitoredProcess https = new MonitoredProcess( "https", MonitorInit.getConfig().getHttps() );
//        HTTPS = new MonitoredServerStateMachine( "https", https );

        // initialize them...
        HTTP.on( Event.INITIALIZE );
//        HTTPS.on( Event.INITIALIZE );

        // watch for console shutdown commands (for debugging in IDE)...
        ShutterDowner sd;
        if( (_args.length > 0) && ("test".equalsIgnoreCase( _args[0] ) ) ) {
            sd = new ShutterDowner();
            sd.start();
        }

        while( !SHUTDOWN ) {
            Thread.sleep( 100 );
        }

        LOG.info( "Shutting down servers" );

        // tell our monitored servers to shut down...
        try {
            IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.Shutdown, null ), IPMsgParticipant.HTTP );
            IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.Shutdown, null ), IPMsgParticipant.HTTPS );
        }
        catch( IOException e ) {
            LOG.error( "Problem sending shutdown message to monitored servers", e );
        }

        Thread.sleep( 1000 );

        if( (HTTP != null) && HTTP.getProcess().isAlive() )
            HTTP.getProcess().stopProcess();
        if( (HTTPS != null) && HTTPS.getProcess().isAlive() )
            HTTPS.getProcess().stopProcess();

        IPMsgSocket.INSTANCE.shutdown();
        ExecutionService.INSTANCE.shutdown();

        LOG.info( "Main thread exiting" );

    }


    public static void shutdown() {
        SHUTDOWN = true;
    }


    public static MonitoredServerStateMachine getHttpStateMachine() {
        return HTTP;
    }


    public static MonitoredServerStateMachine getHttpsStateMachine() {
        return HTTPS;
    }


    public static MonitoredServerStateMachine getStateMachine( final IPMsgParticipant _participant ) {
        return (_participant == IPMsgParticipant.HTTP) ? HTTP : (_participant == IPMsgParticipant.HTTPS) ? HTTPS : null;
    }


    // this class is provided for convenience when debugging, to allow system shutdown from the keyboard
    private static class ShutterDowner extends Thread {

        private ShutterDowner() {
            setName( "shutterdowner" );
            setDaemon( true );
        }

        public void run() {

            BufferedReader bir = new BufferedReader( new InputStreamReader( System.in ) );

            while( !SHUTDOWN ) {
                try {
                    String line = bir.readLine();
                    if( "shutdown".equalsIgnoreCase( line ) )
                        SHUTDOWN = true;
                }
                catch( IOException e ) {
                    LOG.error( "Problem in STDIN reader", e );
                }
            }
        }
    }
}
