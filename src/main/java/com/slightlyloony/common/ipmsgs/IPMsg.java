package com.slightlyloony.common.ipmsgs;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * The base class for all InterProcess Messages (IPMsg).
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class IPMsg implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    private final static List<Class<? extends IPMsg>> typeCodeMap = getMap();

    private final int typeCode;


    public IPMsg( final int _typeCode ) {
        typeCode = _typeCode;
    }


    public byte getTypeCode() {
        return (byte) typeCode;
    }


    public ByteBuffer toMessageBuffer() {

        ByteBuffer result = null;
        try {
            String json = new Gson().toJson( this );
            byte[] jsonBytes = json.getBytes( Charset.forName( "UTF-8" ) );
            result = ByteBuffer.allocate( jsonBytes.length + 1 );
            result.put( (byte)(typeCode & 0xff) );
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

    @Override
    abstract public void run();


    public static IPMsg create( final ByteBuffer _buffer ) {

        try {
            int code = _buffer.get() & 0xff;
            Class<? extends IPMsg> klass = typeCodeMap.get(code);
            CharBuffer jsonBuffer = Charset.forName( "UTF-8" ).decode( _buffer );
            jsonBuffer.position( 0 );
            return (IPMsg) new Gson().fromJson( jsonBuffer.toString(), klass );
        }

        // any exception in the above code will be unchecked, so we catch it and rethrow a summary...
        catch( JsonSyntaxException e ) {
            LOG.error( "Problem while decoding an IPMsg", e );
            throw new IllegalStateException( "Problem decoding an IPMsg", e );
        }
    }


    public static List<Class<? extends IPMsg>> getMap() {
        List<Class<? extends IPMsg>> result = new ArrayList<>();

        result.add( StartHTTPMsg.class    );  // 0
        result.add( StopHTTPMsg.class     );  // 1
        result.add( HTTPAliveMsg.class    );  // 2
        result.add( HTTPSAliveMsg.class   );  // 3

        return result;
    }
}
