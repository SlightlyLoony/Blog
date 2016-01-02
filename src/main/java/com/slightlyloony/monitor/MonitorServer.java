package com.slightlyloony.monitor;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.monitor.state.Event;
import com.slightlyloony.monitor.state.MonitoredServerStateMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitorServer {


    private static MonitoredServerStateMachine HTTP = null;
    private static MonitoredServerStateMachine HTTPS = null;
    private static boolean SHUTDOWN = false;


    public static void main( final String[] _args ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        Logger LOG = LogManager.getLogger();

        // initialization of the monitor application...
        MonitorInit.init();

        // set up our monitored processes...
        MonitoredProcess http = new MonitoredProcess( "http", MonitorInit.getConfig().getHttp() );
        HTTP = new MonitoredServerStateMachine( "http", http );
        MonitoredProcess https = new MonitoredProcess( "https", MonitorInit.getConfig().getHttps() );
        HTTPS = new MonitoredServerStateMachine( "https", https );

        // initialize them...
        HTTP.on( Event.INITIALIZE );
        HTTPS.on( Event.INITIALIZE );

        while( !SHUTDOWN ) {
            Thread.sleep( 1000 );
//            String s = https.getStdOut();
//            if( s.length() > 0)
//                System.out.println( "STDOOUT: " + s );
        }

        LOG.info( "Shutting down servers" );
        if( HTTP.getProcess().isAlive() )
            HTTP.getProcess().stopProcess();
        if( HTTPS.getProcess().isAlive() )
            HTTPS.getProcess().stopProcess();
        ExecutionService.INSTANCE.shutdown();

        Thread.sleep( 10000 );
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
}
