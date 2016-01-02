package com.slightlyloony.redirector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.*;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RedirectorServer {

    private static final int HTTP_RESP_PERMANANTLY_MOVED = 301;

    private static Logger LOG;


    public static void main( final String _args[] ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        LOG = LogManager.getLogger();

        // initialization of the redirector application...
        RedirectorInit.init();

        // now it's time to set up our web server...
        RedirectorConfig rc = RedirectorInit.getConfig();
        port( rc.getPort() );
        threadPool( rc.getMaxThreads(), rc.getMinThreads(), rc.getThreadTimeoutMillis() );


        // just one route; we're redirecting everything...
        get( "/*", (request, response) -> {
            String host = request.host();
            host = host.substring( 0, host.lastIndexOf( ":" ) );
            String path = request.pathInfo();
            String stuff = request.queryString();
            String url = "https://" + host + ":4443" + path;
            if( (stuff != null) && (stuff.length() > 0))
                url += "?" + stuff;
            LOG.info( "URL: " + url );
            response.redirect( url, 301);
            return "";
        } );

        while(true) {
            Thread.sleep( 1000 );
        }
    }
}
