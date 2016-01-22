package com.slightlyloony.blog;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.slightlyloony.blog.config.ServerConfig;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.objects.BlogIDs;
import com.slightlyloony.blog.security.BlogSessionManager;
import com.slightlyloony.blog.storage.CachedStorage;
import com.slightlyloony.blog.storage.Storage;
import com.slightlyloony.common.StandardUncaughtExceptionHandler;
import com.slightlyloony.common.ipmsgs.IPMsgAction;
import com.slightlyloony.common.ipmsgs.IPMsgParticipant;
import com.slightlyloony.common.ipmsgs.IPMsgSocket;
import com.slightlyloony.common.ipmsgs.IPMsgType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            throw new HandlerIllegalStateException( "Could not read blog server configuration", e );
        }

        // initialize our blog object IDs after an integrity check...
        if( !BlogIDs.INSTANCE.integrityCheck() ) {
            LOG.fatal( "Blog object IDs failed integrity check, shutting down system" );
            System.exit( 1 );
        }
        BlogIDs.INSTANCE.init();

        // initialize the storage system...
        BlogServer.STORAGE = new CachedStorage( new Storage( ServerInit.getConfig().getContentRoot() ) );

        // create our blog instances...
        for( String blog : CONFIG.getBlogs() ) {
            Blog blogInstance = Blog.create( blog );
            if( blogInstance != null )
                BlogServer.addBlog( blogInstance );
        }

        // start the session manager...
        BlogSessionManager.INSTANCE.init();

        // start the inter-process message listener...
        IPMsgSocket.start( CONFIG.getHttps().getSocketAddress(), getValidSenders(), getValidMsgs() );
    }


    public static ServerConfig getConfig() {
        return CONFIG;
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
