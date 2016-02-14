package com.slightlyloony.blog.events;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;

import java.time.Instant;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Event {

    private final Instant timestamp;
    private final EventType type;
    private final Object[] params;


    public Event( final EventType _type, Object... _params ) {

        if( _type == null )
            throw new HandlerIllegalArgumentException( "Missing event type" );

        timestamp = Instant.now();
        type = _type;
        params = (_params == null) ? new Object[0] : _params;

        if( params.length != _type.size() )
            throw new HandlerIllegalArgumentException( "Mismatched number of parameters: expected " + _type.size() + ", got " + params.length );

        for( int i = 0; i < params.length; i++ ) {
            if( ! _type.getType( i ).isInstance( params[i] ) )
                throw new HandlerIllegalArgumentException( "Parameter " + i + " should be " + _type.getType( i ).getSimpleName()
                        + ",  was " + params[i].getClass().getSimpleName() );
        }
    }


    public Instant getTimestamp() {
        return timestamp;
    }


    public EventType getType() {
        return type;
    }


    public Object getParam( final int _index ) {
        return ((_index < 0) || (_index >= params.length)) ? null : params[_index];
    }


    public int size() {
        return params.length;
    }
}
