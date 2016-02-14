package com.slightlyloony.blog.events;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum EventType {

    USER_LOGIN        ( String.class ),  // username
    USER_LOGIN_FAILURE( String.class );  // username


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
