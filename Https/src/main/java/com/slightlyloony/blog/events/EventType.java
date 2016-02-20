package com.slightlyloony.blog.events;

import com.slightlyloony.blog.security.BlogSession;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum EventType {

    UNCACHED_READ     ( String.class, Integer.class ),  // cache name, object size
    CACHE_HIT         ( String.class, Integer.class ),  // cache name, object size
    CACHE_MISS        ( String.class, Integer.class ),  // cache name, object size

    INFO_REQUEST      ( String.class                ),  // info key

    PAGE_HIT          ( BlogSession.class ),  // session
    SESSION_KILLED    ( BlogSession.class ),  // session
    USER_LOGIN        ( BlogSession.class ),  // session
    USER_LOGIN_FAILURE( String.class );       // username


    private final Class[] types;


    EventType( final Class... _types ) {
        types = (_types == null) ? new Class[0] : _types;
    }


    public Class getType( final int _index ) {
        return (_index < 0) || (_index >= types.length) ? null :  types[_index];
    }


    public int size() {
        return types.length;
    }
}
