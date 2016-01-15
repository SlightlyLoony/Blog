package com.slightlyloony.blog;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.slightlyloony.blog.config.BlogConfig;
import com.slightlyloony.blog.config.ServerConfig;
import com.slightlyloony.common.StandardUncaughtExceptionHandler;
import com.slightlyloony.common.ipmsgs.IPMsgAction;
import com.slightlyloony.common.ipmsgs.IPMsgParticipant;
import com.slightlyloony.common.ipmsgs.IPMsgSocket;
import com.slightlyloony.common.ipmsgs.IPMsgType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.SocketAddress;
import java.util.Map;

/**
 * Static container class for blog monitor initialization code.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ServerInit {


    private static final Logger LOG = LogManager.getLogger();

    private static ServerConfig CONFIG;
    private static Map<String,BlogConfig> BLOGS;


    public static void init() {

        /*
         * B E   V E R Y   C A R E F U L   H E R E ! ! !
         *
         * In particular, be very cautious about changing the order of initializers.  They're in this particular order for a reason!
         */

        // set a default uncaught exception handler...
        Thread.setDefaultUncaughtExceptionHandler( new StandardUncaughtExceptionHandler() );

        // read the blog server's configuration...
        try {
            CONFIG = new Gson().fromJson( new FileReader( "server.json" ), ServerConfig.class );
        }
        catch( FileNotFoundException e ) {
            LOG.fatal( "Problem reading blog server configuration", e );
            throw new IllegalStateException( "Could not read blog server configuration", e );
        }

        // read the configuration for our blogs...
        File contentRoot = new File( CONFIG.getContentRoot() );
        BLOGS = Maps.newHashMap();
        for( String blog : CONFIG.getBlogs() ) {

            try {
                File blogRoot = new File( contentRoot, blog );
                File blogConfigFile = new File( blogRoot, "blog.json");
                BlogConfig blogConfig = new Gson().fromJson( new FileReader( blogConfigFile ), BlogConfig.class );
                BLOGS.put( blog, blogConfig );
            }
            catch( FileNotFoundException e ) {
                LOG.fatal( "Problem reading blog configuration", e );
                throw new IllegalStateException( "Could not read blog configuration", e );
            }
        }

        // start the inter-process message listener...
        IPMsgSocket.start( CONFIG.getHttps().getSocketAddress(), getValidSenders(), getValidMsgs() );
    }


    public static ServerConfig getConfig() {
        return CONFIG;
    }


    public static BlogConfig getBlogConfig( final String _blogName ) {
        return BLOGS.get( _blogName );
    }


    /*
     * Prevent instantiation.
     */
    private ServerInit(){}


    private static Map<SocketAddress,IPMsgParticipant> getValidSenders() {
        Map<SocketAddress,IPMsgParticipant> result = Maps.newHashMap();
        result.put( CONFIG.getMonitor().getSocketAddress(), IPMsgParticipant.MONITOR );
        return result;
    }


    private static Map<IPMsgType,IPMsgAction> getValidMsgs() {

        Map<IPMsgType,IPMsgAction> result = Maps.newHashMap();

        result.put( IPMsgType.StartWeb, ( _participant, _data ) -> BlogServer.start() );
        result.put( IPMsgType.StopWeb, ( _participant, _data ) -> BlogServer.stop() );
        result.put( IPMsgType.Shutdown, ( _participant, _data ) -> BlogServer.shutdown() );

        return result;
    }
}
