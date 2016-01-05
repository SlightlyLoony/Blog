package com.slightlyloony.redirector;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.common.ipmsgs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RedirectorServer {

    private static final int HTTP_RESP_PERMANANTLY_MOVED = 301;

    private static Logger LOG;

    private static ScheduledFuture aliveMessageFuture;

    private static long sequence = 0;

    private static boolean shutdown = false;


    public static void main( final String _args[] ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        LOG = LogManager.getLogger();

        // initialization of the redirector application...
        RedirectorInit.init();

        // tell the monitor we're alive, every fifteen seconds...
        setupAliveMessages();

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
        port( rc.getPort() );
        threadPool( rc.getMaxThreads(), rc.getMinThreads(), rc.getThreadTimeoutMillis() );

        // just one route; we're redirecting everything...
        get( "/*", RedirectorServer::redirect );

        // send a web alive message when we are fully started up...
        Runnable cmd = () -> {
            awaitInitialization();
            try {
                IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.WebAlive, null ), IPMsgParticipant.MONITOR );
            }
            catch( Exception e ) {
                LOG.error( "Problem sending web alive message to monitor", e );
            }
        };
        ExecutionService.INSTANCE.submit( cmd );
    }


    private static Object redirect( final Request request, final Response response ) {
        String host = request.host();
        host = host.substring( 0, host.lastIndexOf( ":" ) );
        String path = request.pathInfo();
        String stuff = request.queryString();
        String url = "https://" + host + ":4443" + path;
        if( (stuff != null) && (stuff.length() > 0))
            url += "?" + stuff;
        LOG.info( "URL: " + url );
        response.redirect( url, HTTP_RESP_PERMANANTLY_MOVED );
        return "";
    }


    public static void stop() {
        LOG.info( "Stopping HTTP server" );
        spark.Spark.stop();
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
