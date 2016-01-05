package com.slightlyloony.common.ipmsgs;

import com.google.common.collect.Maps;
import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Singleton class that implements a UDP listener for interprocess messages.  This class runs in its own thread, and received messages processed
 * in ExecutionService threads.  Messages that did not originate with a valid sender, or which do not resolve to valid messages, are ignored.
 * <p>
 * On the wire, IPMsgs are encoded as follows: one byte of message type code (using the value of IPMsgType), followed, optionally, by a data encoding
 * consisting of a zero-terminated (C-style) string that is the fully-qualified class name of an IPData child class, followed by the JSON-encoded
 * value of an instance of that class.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IPMsgSocket {

    private static final Logger LOG = LogManager.getLogger();

    public static IPMsgSocket INSTANCE;


    private final SocketAddress listenerAddr;
    private final Map<SocketAddress,IPMsgParticipant> validSenders;
    private final Map<IPMsgParticipant,SocketAddress> validRecipients;
    private final Map<IPMsgType,IPMsgAction> validMsgs;
    private final Listener listener;
    private DatagramSocket socket;
    private volatile boolean shutdown = false;


    private IPMsgSocket( final SocketAddress _listenerAddr, final Map<SocketAddress,IPMsgParticipant> _validSenders,
                         final Map<IPMsgParticipant,SocketAddress> _validRecipients, final Map<IPMsgType,IPMsgAction> _validMsgs ) {
        listenerAddr = _listenerAddr;
        validSenders = _validSenders;
        validRecipients = _validRecipients;
        validMsgs = _validMsgs;
        listener = new Listener();
        try {
            socket = new DatagramSocket( listenerAddr );
        }
        catch( SocketException e ) {
            LOG.error( "Problem creating IPMsg DatagramSocket", e );
        }
    }


    public static void start( final SocketAddress _listenerAddr, final Map<SocketAddress,IPMsgParticipant> _validSenders,
                              final Map<IPMsgType,IPMsgAction> _validMsgs ) {

        if( INSTANCE != null )
            return;

        if( (_listenerAddr == null) || (_validSenders == null) || (_validMsgs == null) ) {
            LOG.error( "Invalid arguments to IPMsgSocket.start()" );
            throw new IllegalArgumentException( "Invalid arguments to IPMsgSocket.start()" );
        }

        // invert the valid senders to get valid recipients...
        Map<IPMsgParticipant,SocketAddress> validRecipients = Maps.newHashMap();
        for( SocketAddress socket : _validSenders.keySet() )
            validRecipients.put( _validSenders.get( socket ), socket );

        INSTANCE = new IPMsgSocket( _listenerAddr, _validSenders, validRecipients, _validMsgs );
        INSTANCE.listener.start();
    }


    public void send( final IPMsg _msg, final IPMsgParticipant _participant ) throws IOException {

        // get the socket address of our destination...
        SocketAddress socketAddress = validRecipients.get( _participant );
        if( socket == null )
            throw new IllegalArgumentException( "Unknown recipient: " + _participant );

        ByteBuffer bb = _msg.encode();
        byte[] b = new byte[bb.limit()];
        bb.get( b );
        DatagramPacket dp = new DatagramPacket( b, b.length, socketAddress );
        socket.send( dp );
    }


    public void shutdown() {
        shutdown = true;
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
            while( !shutdown && !interrupted() ) {

                try {

                    // some setup...
                    ByteBuffer bb = ByteBuffer.allocate( 600 );

                    // we basically do this forever, once we've started...
                    while( !shutdown && !interrupted() ) {

                        // wait for the arrival of a UDP packet...
                        byte[] buffer = new byte[600];
                        DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
                        try {
                            socket.receive( packet );
                        }
                        catch( IOException e ) {
                            if( !shutdown )
                                LOG.error( "Retrying after failure while listening", e );
                            continue;
                        }

                        // if this packet didn't come from a friend, we just ignore it...
                        final IPMsgParticipant friend = validSenders.get( packet.getSocketAddress() );
                        if( friend == null ) {
                            LOG.info( LU.msg( "Ignoring packet received from {0}", packet.getSocketAddress().toString() ) );
                            continue;
                        }

                        // decode the message...
                        final IPMsg msg;
                        try {

                            // get the message...
                            bb.clear();
                            bb.put( packet.getData(), 0, packet.getLength() );
                            bb.flip();
                            msg = IPMsg.decode( bb );
                        }
                        catch( Exception e ) {
                            LOG.error( "Problem decoding received message", e );
                            continue;
                        }

                        // if this message isn't one we're prepared to process, ignore it...
                        final IPMsgAction action = validMsgs.get( msg.getType() );
                        if( action == null ) {
                            LOG.info( LU.msg( "Ignoring message with unexpected type {0}", msg.getType() ) );
                            continue;
                        }

                        // if we get here, then we received a valid message from a valid sender, with the right data type - so process it!
                        LOG.info( LU.msg( "Processing valid message \"{0}\" received from {1}",
                                msg.getType(), packet.getSocketAddress().toString() ) );
                        Runnable runIt = new Runnable() {
                            @Override
                            public void run() {
                                action.run( friend, msg.getData() );
                            }
                        };
                        ExecutionService.INSTANCE.submit( runIt );
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
