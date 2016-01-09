package com.slightlyloony.redirector;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.common.ipmsgs.*;
import com.slightlyloony.common.logging.Jetty2Log4j2Bridge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RedirectorServer {

    private static Logger LOG;

    private static ScheduledFuture aliveMessageFuture;
    private static long sequence = 0;
    private static boolean shutdown = false;
    private static Server server;


    public static void main( final String _args[] ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        LOG = LogManager.getLogger();

        // initialization of the redirector application...
        RedirectorInit.init();

        // tell the monitor we're alive, every fifteen seconds...
        setupAliveMessages();

        // if we're in standalone test mode (presumably in an IDE for debugging), start up the web server...
        if( (_args.length > 0) && ("test".equals( _args[0] ) ) )
            start();

        while( !shutdown ) {
            Thread.sleep( 100 );
        }

        LOG.info( "Shutting down server" );
        try {
            IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.ShuttingDown, null ), IPMsgParticipant.MONITOR );
        }
        catch( IOException e ) {
            LOG.error( "Problem sending shutting down message to monitor", e );
        }

        Thread.sleep( 1000 );
        IPMsgSocket.INSTANCE.shutdown();
        ExecutionService.INSTANCE.shutdown();
    }


    public static void start() {

        LOG.info( "Starting HTTP server" );

        RedirectorConfig rc = RedirectorInit.getConfig();

        Log.setLog( new Jetty2Log4j2Bridge( RedirectorServer.class.getName() ));
        server = new Server( rc.getPort() );
        server.setHandler( new RedirectHandler() );
        try {
            server.start();
        }
        catch( Exception e ) {
            LOG.error( "Jetty HTTP server could not start", e );
        }
        server.dumpStdErr();



        // send a web alive message when we are fully started up...
        Runnable cmd = () -> {
//            awaitInitialization();
            try {
                IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.WebAlive, null ), IPMsgParticipant.MONITOR );
            }
            catch( Exception e ) {
                LOG.error( "Problem sending web alive message to monitor", e );
            }
        };
        ExecutionService.INSTANCE.submit( cmd );
    }


    public static void stop() {
        aliveMessageFuture.cancel( true );
        LOG.info( "Stopping HTTP server" );
        try {
            server.stop();
        }
        catch( Exception e ) {
            LOG.error( "Jetty HTTP server could not stop", e );
        }
    }


    public static void shutdown() {
        stop();
        shutdown = true;
    }


    private static void setupAliveMessages() {

        Runnable cmd = () -> {
            try {
                IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.ProcessAlive, new IPSequenceData( sequence++ ) ),IPMsgParticipant.MONITOR );
            }
            catch( IOException e ) {
                LOG.error( "Could not send HTTPAliveMsg to monitor", e );
            }
        };
        aliveMessageFuture = ExecutionService.INSTANCE.scheduleAtFixedRate( cmd, 0, 15, TimeUnit.SECONDS );
    }
}
