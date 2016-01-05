package com.slightlyloony.monitor;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.slightlyloony.common.StandardUncaughtExceptionHandler;
import com.slightlyloony.common.ipmsgs.*;
import com.slightlyloony.common.logging.LU;
import com.slightlyloony.monitor.state.Event;
import com.slightlyloony.monitor.state.MonitoredServerStateMachine;
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
public class MonitorInit {


    private static final Logger LOG = LogManager.getLogger();

    private static MonitorConfig CONFIG;


    public static void init() {

        /*
         * B E   V E R Y   C A R E F U L   H E R E ! ! !
         *
         * In particular, be very cautious about changing the order of initializers.  They're in this particular order for a reason!
         */

        // set a default uncaught exception handler...
        Thread.setDefaultUncaughtExceptionHandler( new StandardUncaughtExceptionHandler() );

        // read the monitor's configuration...
        try {
            CONFIG = new Gson().fromJson( new FileReader( "monitor.json" ), MonitorConfig.class );
        }
        catch( FileNotFoundException e ) {
            LOG.fatal( "Problem reading monitor configuration", e );
            throw new IllegalStateException( "Could not read monitor configuration", e );
        }

        // start the mail portal...
        MailPortal.start();

        // start the inter-process message listener...
        IPMsgSocket.start( CONFIG.getMonitor().getSocketAddress(), getValidSenders(), getValidMsgs() );
    }


    public static MonitorConfig getConfig() {
        return CONFIG;
    }


    /*
     * Prevent instantiation.
     */
    private MonitorInit(){}


    private static Map<SocketAddress,IPMsgParticipant> getValidSenders() {
        Map<SocketAddress,IPMsgParticipant> result = Maps.newHashMap();
        result.put( CONFIG.getHttp().getSocketAddress(),  IPMsgParticipant.HTTP  );
        result.put( CONFIG.getHttps().getSocketAddress(), IPMsgParticipant.HTTPS );
        return result;
    }


    private static Map<IPMsgType,IPMsgAction> getValidMsgs() {

        Map<IPMsgType,IPMsgAction> result = Maps.newHashMap();

        result.put( IPMsgType.WebAlive, ( _participant, _data ) -> {

            MonitoredServerStateMachine machine = MonitorServer.getStateMachine( _participant );

            // if we have a state machine to inform...
            if( machine != null) {

                // then fire off an event...
                machine.on( Event.WEB_ALIVE );
            }

        } );

        result.put( IPMsgType.ProcessAlive, ( _participant, _data ) -> {

            MonitoredServerStateMachine machine = MonitorServer.getStateMachine( _participant );

            // if we have a state machine to inform...
            if( machine != null) {

                // then fire off an event...
                machine.on( Event.ALIVE );
            }
        } );

        result.put( IPMsgType.ShuttingDown, ( _participant, _data ) -> LOG.info( LU.msg( "Monitored server {0} is shutting down", _participant ) ) );

        return result;
    }
}
