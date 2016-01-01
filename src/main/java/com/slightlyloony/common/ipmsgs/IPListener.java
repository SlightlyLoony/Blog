package com.slightlyloony.common.ipmsgs;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Singleton class that implements a UDP listener for interprocess messages.  This class runs in its own thread, and received messages processed
 * in ExecutionService threads.  Messages that did not originate with a valid sender, or which do not resolve to valid messages, are ignored.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IPListener {

    private static final Logger LOG = LogManager.getLogger();

    private static IPListener INSTANCE;


    private final SocketAddress listenerAddr;
    private final Set<SocketAddress> validSenderAddrs;
    private final Set<Class<? extends IPMsg>> validMsgs;
    private final Listener listener;


    private IPListener( final SocketAddress _listenerAddr, final Set<SocketAddress> _validSenderAddrs, final Set<Class<? extends IPMsg>> _validMsgs ) {
        listenerAddr = _listenerAddr;
        validSenderAddrs = _validSenderAddrs;
        validMsgs = _validMsgs;
        listener = new Listener();
    }


    public static void start( final SocketAddress _listenerAddr, final Set<SocketAddress> _validSenderAddrs, final Set<Class<? extends IPMsg>> _validMsgs ) {

        if( INSTANCE != null )
            return;

        if( (_listenerAddr == null) || (_validSenderAddrs == null) || (_validMsgs == null) ) {
            LOG.error( "Invalid arguments to IPListener.start()" );
            throw new IllegalArgumentException( "Invalid arguments to IPListener.start()" );
        }

        INSTANCE = new IPListener( _listenerAddr, _validSenderAddrs, _validMsgs );
        INSTANCE.listener.start();
    }


    private class Listener extends Thread {

        private Listener() {
            setName( "IPListener" );
            setDaemon( true );
        }


        @Override
        public void run() {

            LOG.info( LU.msg( "Starting IP listener on {0}", listenerAddr.toString() ) );

            // we basically do this forever, once we've started...
            while( !interrupted() ) {

                DatagramSocket socket = null;

                try {

                    // some setup...
                    socket = new DatagramSocket( listenerAddr );
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
                        LOG.info( LU.msg( "Processing valid message \"{0}\" received from {1}", msg.getClass().getSimpleName(), packet.getSocketAddress().toString() ) );
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
