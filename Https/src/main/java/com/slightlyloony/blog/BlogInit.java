package com.slightlyloony.blog;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
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
public class BlogInit {


    private static final Logger LOG = LogManager.getLogger();

    private static BlogConfig CONFIG;


    public static void init() {

        /*
         * B E   V E R Y   C A R E F U L   H E R E ! ! !
         *
         * In particular, be very cautious about changing the order of initializers.  They're in this particular order for a reason!
         */

        // set a default uncaught exception handler...
        Thread.setDefaultUncaughtExceptionHandler( new StandardUncaughtExceptionHandler() );

        // read the redirector's configuration...
        try {
            CONFIG = new Gson().fromJson( new FileReader( "blog.json" ), BlogConfig.class );
        }
        catch( FileNotFoundException e ) {
            LOG.fatal( "Problem reading blog configuration", e );
            throw new IllegalStateException( "Could not read blog configuration", e );
        }

        // start the inter-process message listener...
        IPMsgSocket.start( CONFIG.getHttps().getSocketAddress(), getValidSenders(), getValidMsgs() );
    }


    public static BlogConfig getConfig() {
        return CONFIG;
    }


    /*
     * Prevent instantiation.
     */
    private BlogInit(){}


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
