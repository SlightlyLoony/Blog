package com.slightlyloony.blog.server;

import com.slightlyloony.common.ipmsgs.IPMsg;
import com.slightlyloony.common.ipmsgs.HTTPAliveMsg;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Server {

    public static void main( final String[] _args ) throws IOException {
        IPMsg msg = new HTTPAliveMsg();
        ByteBuffer bb = msg.toMessageBuffer();
        byte[] b = new byte[bb.limit()];
        bb.get( b );
        DatagramPacket dp = new DatagramPacket( b, b.length, new InetSocketAddress( "127.0.0.1", 54321 ) );
        DatagramSocket socket = new DatagramSocket( new InetSocketAddress( "127.0.0.1", 54323 ));
        socket.send( dp );
        socket.close();
    }
}
