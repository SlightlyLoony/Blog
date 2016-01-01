package com.slightlyloony.common.ipmsgs;

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

    }
}
