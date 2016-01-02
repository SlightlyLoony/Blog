package com.slightlyloony.common.ipmsgs;

import com.slightlyloony.monitor.state.Event;

/**
 * Sent periodically by the HTTP and HTTPS servers to tell the monitor that they are alive.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HTTPSAliveMsg extends IPMsg {


    public HTTPSAliveMsg() {
        super( 3 );
    }


    @Override
    public void run() {

        // if we have a state machine to inform...
        if( httpsSM() != null) {

            // then fire off an event...
            httpsSM().on( Event.ALIVE );
        }
    }
}
