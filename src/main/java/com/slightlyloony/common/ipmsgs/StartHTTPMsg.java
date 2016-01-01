package com.slightlyloony.common.ipmsgs;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StartHTTPMsg extends IPMsg {

    private final String id;


    public StartHTTPMsg( final String _id ) {
        super( 0 );
        id = _id;
    }


    @Override
    public void run() {

    }


    public String getId() {
        return id;
    }
}
