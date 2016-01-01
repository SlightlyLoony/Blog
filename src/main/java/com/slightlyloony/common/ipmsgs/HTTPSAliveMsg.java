package com.slightlyloony.common.ipmsgs;

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

    }
}
