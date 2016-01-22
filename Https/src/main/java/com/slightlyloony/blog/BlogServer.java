package com.slightlyloony.blog;

import com.google.common.collect.Maps;
import com.slightlyloony.blog.config.ServerConfig;
import com.slightlyloony.blog.handlers.BlogHandler;
import com.slightlyloony.blog.storage.CachedStorage;
import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.common.ipmsgs.*;
import com.slightlyloony.common.logging.Jetty2Log4j2Bridge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogServer {

    public static CachedStorage STORAGE;

    private static Logger LOG;

    private static ScheduledFuture aliveMessageFuture;
    private static long sequence = 0;
    private static boolean shutdown = false;
    private static Server server;
    private static Map<String,Blog> blogs = Maps.newHashMap();



    public static void main( final String _args[] ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        LOG = LogManager.getLogger();

        // initialize the blog application...
        ServerInit.init();

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

        LOG.info( "Starting HTTPS server" );

        Log.setLog( new Jetty2Log4j2Bridge( BlogServer.class.getName() ) );

        try {
            server = new Server();
            server.setDumpAfterStart( false );
            server.setDumpBeforeStop( false );
            server.setStopAtShutdown( true );
            server.setConnectors( getConnectors() );
            server.setHandler( new BlogHandler() );

            // Start the server
            server.start();
            server.dumpStdErr();

            LOG.info( "HTTPS server starting" );
        }
        catch( Throwable e ) {
            LOG.error( "Jetty HTTPS server could not start", e );
        }



        // send a web alive message when we are fully started up...
        Runnable cmd = () -> {

            LOG.info( "Waiting for HTTPS server to start" );

            server.isRunning();
            try {

                LOG.info( "Sending HTTPS WebAlive message" );

                IPMsgSocket.INSTANCE.send( new IPMsg( IPMsgType.WebAlive, null ), IPMsgParticipant.MONITOR );
            }
            catch( Exception e ) {
                LOG.error( "Problem sending web alive message to monitor", e );
            }
        };
        ExecutionService.INSTANCE.submit( cmd );
    }


    private static Connector[] getConnectors() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        // iterate through all our blogs, configuring connectors for them...
        ServerConfig serverConfig = ServerInit.getConfig();
        Connector[] connectors = new Connector[blogs.size()];
        int i = 0;
        for( Blog blog : blogs.values() ) {

            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setSecureScheme( "https" );
            http_config.setSecurePort( blog.getConfig().getPort() );

            HttpConfiguration https_config = new HttpConfiguration( http_config );
            https_config.addCustomizer( new SecureRequestCustomizer() );

            SslContextFactory sslContextFactory = new SslContextFactory();

            // loaded keystore instead of just supplying path; workaround for Java 1.8 bug:  JDK-7181721
            // see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7181721
            // spent a day tracking this down!!!
            KeyStore ks = KeyStore.getInstance( "JKS" );
            InputStream readStream = new FileInputStream( serverConfig.getKeystore() );
            ks.load( readStream, serverConfig.getKeystorePassword().toCharArray() );
            readStream.close();
            sslContextFactory.setKeyStore( ks );

            sslContextFactory.setKeyStorePassword( serverConfig.getKeystorePassword() );
            sslContextFactory.setCertAlias( blog.getConfig().getCertAlias() );

            ServerConnector httpsConnector = new ServerConnector(
                    server,
                    new SslConnectionFactory( sslContextFactory, "http/1.1" ),
                    new HttpConnectionFactory( https_config )
            );
            httpsConnector.setPort( blog.getConfig().getPort() );
            httpsConnector.setIdleTimeout( 50000 );
            connectors[i++] = httpsConnector;
        }
        return connectors;
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
                LOG.error( "Could not send ProcessAlive to monitor", e );
            }
        };
        aliveMessageFuture = ExecutionService.INSTANCE.scheduleAtFixedRate( cmd, 0, 15, TimeUnit.SECONDS );
    }


    public static void addBlog( final Blog _blog ) {
        blogs.put( _blog.getHost(), _blog );
    }


    public static Blog getBlog( final String _host ) {
        return blogs.get( _host );
    }
}
