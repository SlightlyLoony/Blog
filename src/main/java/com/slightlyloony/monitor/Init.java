package com.slightlyloony.monitor;

import com.google.gson.Gson;
import com.slightlyloony.common.ipmsgs.IPListener;
import com.slightlyloony.common.ipmsgs.IPMsg;
import com.slightlyloony.common.ipmsgs.HTTPAliveMsg;
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
public class Init {


    private static final Logger LOG = LogManager.getLogger();

    private static MonitorConfig CONFIG;


    public static void init() {

        /*
         * B E   V E R Y   C A R E F U L   H E R E ! ! !
         *
         * In particular, be very cautious about changing the order of initializers.  They're in this particular order for a reason!
         */

        // set a default uncaught exception handler...
        Thread.setDefaultUncaughtExceptionHandler( new MonitorUncaughtExceptionHandler() );

        // read the monitor's configuration...
        try {
            CONFIG = new Gson().fromJson( new FileReader( "PrivateStuff/monitor.json" ), MonitorConfig.class );
        }
        catch( FileNotFoundException e ) {
            LOG.fatal( "Problem reading monitor configuration", e );
            throw new IllegalStateException( "Could not read monitor configuration", e );
        }

        // start the mail portal...
        MailPortal.start();

        // start the inter-process message listener...
        IPListener.start( CONFIG.getMonitorIPSocketAddress(), getValidSenders(), getValidMsgs() );
    }


    public static MonitorConfig getConfig() {
        return CONFIG;
    }


    /*
     * Prevent instantiation.
     */
    private Init(){}


    private static Set<SocketAddress> getValidSenders() {
        Set<SocketAddress> result = new HashSet<>();
        result.add( CONFIG.getHttpIPSocketAddress() );
        result.add( CONFIG.getHttpsIPSocketAddress() );
        return result;
    }


    private static Set<Class<? extends IPMsg>> getValidMsgs() {
        Set<Class<? extends IPMsg>> result = new HashSet<>();
        result.add( HTTPAliveMsg.class );
        return result;
    }
}
