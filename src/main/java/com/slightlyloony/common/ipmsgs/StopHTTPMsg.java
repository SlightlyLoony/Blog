package com.slightlyloony.common.ipmsgs;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StopHTTPMsg extends IPMsg {

    private final String id;


    public StopHTTPMsg( final String _id ) {
        super( 1 );
        id = _id;
    }


    @Override
    public void run() {

    }


    public String getId() {
        return id;
    }
}
