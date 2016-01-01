package com.slightlyloony.common.ipmsgs;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IPMsgTest {

    @Test
    public void simple() {

        IPMsg msg1 = new StartHTTPMsg( "157891023985y67" );
        ByteBuffer buffer = msg1.toMessageBuffer();
        IPMsg msg2 = IPMsg.create( buffer );
        hashCode();
    }
}