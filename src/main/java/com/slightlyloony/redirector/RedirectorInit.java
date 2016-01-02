package com.slightlyloony.redirector;

import com.google.gson.Gson;
import com.slightlyloony.common.StandardUncaughtExceptionHandler;
import com.slightlyloony.common.ipmsgs.IPMsg;
import com.slightlyloony.common.ipmsgs.IPMsgSocket;
import com.slightlyloony.common.ipmsgs.StartHTTPMsg;
import com.slightlyloony.common.ipmsgs.StopHTTPMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Static container class for blog monitor initialization code.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RedirectorInit {


    private static final Logger LOG = LogManager.getLogger();

    private static RedirectorConfig CONFIG;


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
            CONFIG = new Gson().fromJson( new FileReader( "redirector.json" ), RedirectorConfig.class );
        }
        catch( FileNotFoundException e ) {
            LOG.fatal( "Problem reading monitor configuration", e );
            throw new IllegalStateException( "Could not read monitor configuration", e );
        }

        // start the inter-process message listener...
        IPMsgSocket.start( CONFIG.getHttp().getSocketAddress(), getValidSenders(), getValidMsgs() );
    }


    public static RedirectorConfig getConfig() {
        return CONFIG;
    }


    /*
     * Prevent instantiation.
     */
    private RedirectorInit(){}


    private static Set<SocketAddress> getValidSenders() {
        Set<SocketAddress> result = new HashSet<>();
        result.add( CONFIG.getMonitor().getSocketAddress() );
        return result;
    }


    private static Set<Class<? extends IPMsg>> getValidMsgs() {
        Set<Class<? extends IPMsg>> result = new HashSet<>();
        result.add( StartHTTPMsg.class );
        result.add( StopHTTPMsg.class );
        return result;
    }
}
