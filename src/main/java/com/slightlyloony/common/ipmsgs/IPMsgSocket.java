package com.slightlyloony.common.ipmsgs;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Singleton class that implements a UDP listener for interprocess messages.  This class runs in its own thread, and received messages processed
 * in ExecutionService threads.  Messages that did not originate with a valid sender, or which do not resolve to valid messages, are ignored.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
//TODO: doesn't shut down correctly; thread still running after main exits
public class IPMsgSocket {

    private static final Logger LOG = LogManager.getLogger();

    public static IPMsgSocket INSTANCE;


    private final SocketAddress listenerAddr;
    private final Set<SocketAddress> validSenderAddrs;
    private final Set<Class<? extends IPMsg>> validMsgs;
    private final Listener listener;
    private DatagramSocket socket;


    private IPMsgSocket( final SocketAddress _listenerAddr, final Set<SocketAddress> _validSenderAddrs,
                         final Set<Class<? extends IPMsg>> _validMsgs ) {
        listenerAddr = _listenerAddr;
        validSenderAddrs = _validSenderAddrs;
        validMsgs = _validMsgs;
        listener = new Listener();
        try {
            socket = new DatagramSocket( listenerAddr );
        }
        catch( SocketException e ) {
            LOG.error( "Problem creating IPMsg DatagramSocket", e );
        }
    }


    public void send( final IPMsg _msg, final SocketAddress _addr ) throws IOException {

        ByteBuffer bb = _msg.toMessageBuffer();
        byte[] b = new byte[bb.limit()];
        bb.get( b );
        DatagramPacket dp = new DatagramPacket( b, b.length, _addr );
        socket.send( dp );
    }


    public static void start( final SocketAddress _listenerAddr, final Set<SocketAddress> _validSenderAddrs,
                              final Set<Class<? extends IPMsg>> _validMsgs ) {

        if( INSTANCE != null )
            return;

        if( (_listenerAddr == null) || (_validSenderAddrs == null) || (_validMsgs == null) ) {
            LOG.error( "Invalid arguments to IPMsgSocket.start()" );
            throw new IllegalArgumentException( "Invalid arguments to IPMsgSocket.start()" );
        }

        INSTANCE = new IPMsgSocket( _listenerAddr, _validSenderAddrs, _validMsgs );
        INSTANCE.listener.start();
    }


    public void shutdown() {
        listener.interrupt();
        socket.close();
    }


    private class Listener extends Thread {

        private Listener() {
            setName( "IPMsgSocket" );
            setDaemon( true );
        }


        @Override
        public void run() {

            LOG.info( LU.msg( "Starting IP listener on {0}", listenerAddr.toString() ) );

            // we basically do this forever, once we've started...
            while( !interrupted() ) {

                try {

                    // some setup...
                    ByteBuffer bb = ByteBuffer.allocate( 600 );

                    // we basically do this forever, once we've started...
                    while( !interrupted() ) {

                        // wait for the arrival of a UDP packet...
                        byte[] buffer = new byte[600];
                        DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
                        try {
                            socket.receive( packet );
                        }
                        catch( IOException e ) {
                            LOG.error( "Retrying after failure while listening", e );
                            continue;
                        }

                        // if this packet didn't come from a friend, we just ignore it...
                        if( !validSenderAddrs.contains( packet.getSocketAddress() ) ) {
                            LOG.info( LU.msg( "Ignoring packet received from {0}", packet.getSocketAddress().toString() ) );
                            continue;
                        }

                        // decode the message...
                        IPMsg msg;
                        try {
                            bb.clear();
                            bb.put( packet.getData(), 0, packet.getLength() );
                            bb.flip();
                            msg = IPMsg.create( bb );
                        }
                        catch( Exception e ) {
                            LOG.error( "Problem decoding received message", e );
                            continue;
                        }

                        // if this message isn't one we're prepared to process, ignore it...
                        if( !validMsgs.contains( msg.getClass() ) ) {
                            LOG.info( LU.msg( "Ignoring message with unexpected type {0}", msg.getClass().getSimpleName() ) );
                            continue;
                        }

                        // if we get here, then we received a valid message from a valid sender - process it!
                        LOG.info( LU.msg( "Processing valid message \"{0}\" received from {1}",
                                msg.getClass().getSimpleName(), packet.getSocketAddress().toString() ) );
                        ExecutionService.INSTANCE.submit( msg );
                    }
                }

                catch( Exception e ) {

                    if( (socket != null) && (!socket.isClosed()) ) {
                        socket.close();
                    }

                    LOG.error( "Restarting listener after exception", e );
                }
            }
        }
    }
}
