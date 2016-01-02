package com.slightlyloony.common.ipmsgs;

import com.slightlyloony.monitor.state.Event;

/**
 * Sent periodically by the HTTP and HTTPS servers to tell the monitor that they are alive.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HTTPAliveMsg extends IPMsg {


    public HTTPAliveMsg() {
        super( 2 );
    }


    @Override
    public void run() {

        // if we have a state machine to inform...
        if( httpSM() != null) {

            // then fire off an event...
            httpSM().on( Event.ALIVE );
        }
    }
}
