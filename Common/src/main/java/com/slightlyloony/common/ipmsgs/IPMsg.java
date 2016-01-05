package com.slightlyloony.common.ipmsgs;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static java.text.MessageFormat.format;

/**
 * The base class for all InterProcess Messages (IPMsg).
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IPMsg {

    private static final Logger LOG = LogManager.getLogger();

    private final IPMsgType type;
    private final IPData data;


    public IPMsg( final IPMsgType _type, final IPData _data ) {

        if( _type == null )
            throw new IllegalArgumentException( "Attempted to create IPMsg with no type" );

        type = _type;
        data = _data;
    }


    public IPMsgType getType() {
        return type;
    }


    public IPData getData() {
        return data;
    }


    public ByteBuffer encode() {

        ByteBuffer result = null;

        try {

            // if we have the trivial case of a no-data message, take a shortcut...
            if( type.getFqDataClassName() == null ) {
                result = ByteBuffer.allocate( 1 );
                result.put( (byte) type.getOrdinal() );
                result.flip();
                return result;
            }

            // otherwise, encode the type code, data class name, and data JSON...
            byte[] fqdcnBytes = type.getFqDataClassName().getBytes( "UTF-8" );

            String json = new Gson().toJson( data );
            byte[] jsonBytes = json.getBytes( Charset.forName( "UTF-8" ) );

            result = ByteBuffer.allocate( 1 + fqdcnBytes.length + 1 + jsonBytes.length );
            result.put( (byte) type.getOrdinal() );
            result.put( fqdcnBytes );
            result.put( (byte) 0 );
            result.put( jsonBytes );
            result.flip();
        }

        // any exception in the above code will be unchecked, so we catch it and rethrow a summary...
        catch( Exception e ) {
            LOG.error( "Problem while encoding an IPMsg to JSON", e.getMessage() );
            throw new IllegalStateException( "Problem encoding an IPMsg", e );
        }

        return result;
    }


    public static IPMsg decode( final ByteBuffer _buffer ) {

        try {

            // get the message type code and make sure it's a type we know about...
            int typeCode = 0xff & _buffer.get();
            IPMsgType inboundType = IPMsgType.fromOrdinal( typeCode );
            if( inboundType == null )
                throw new IllegalStateException( "Inbound IPMsg with unknown type code: " + typeCode );

            // if there's nothing left in our buffer, then we have a no-data message and we can take a shortcut...
            if( !_buffer.hasRemaining() ) {

                // if our type is supposed to have data, but doesn't, we've got a problem...
                if( inboundType.getFqDataClassName() != null )
                    throw new IllegalStateException( "Inbound IPMsg should have data, but doesn't" );

                // all is ok, so return our message...
                return new IPMsg( inboundType, null );
            }

            // otherwise, we need to decode it all...
            // first, find the terminator for the fully-qualified data class name...
            int terminator;
            //noinspection StatementWithEmptyBody
            for( terminator = 1; _buffer.get( terminator ) != 0; terminator++ );

            // get the class object for our data class...
            byte[] dcnBytes = new byte[terminator - 1];
            _buffer.get( dcnBytes );
            String fqdcn = new String( dcnBytes, "UTF-8" );
            Class<? extends IPData> klass = (Class<? extends IPData>) Class.forName( fqdcn ).asSubclass( IPData.class );

            // get past the terminator...
            _buffer.get();

            // now decode the JSON and get our data class instance...
            byte[]  jsonBytes = new byte[_buffer.remaining()];
            _buffer.get( jsonBytes );
            String json = new String( jsonBytes, "UTF-8" );
            IPData data = new Gson().fromJson( json, klass );

            // if we got the wrong kind of data, we've got a problem...
            if( !data.getClass().getName().equals( inboundType.getFqDataClassName() ))
                throw new IllegalStateException( format( "Inbound message has data of type {0}; should be {1}",
                        inboundType.getFqDataClassName(), data.getClass().getName() ) );

            // we've got data that matches the type, so return our new message...
            return new IPMsg( inboundType, data );
        }

        // any exception in the above code is caught, logged, and a summary rethrown...
        catch( Exception e ) {
            LOG.error( "Problem while decoding an IPMsg", e );
            throw new IllegalStateException( "Problem decoding an IPMsg", e );
        }
    }
}
