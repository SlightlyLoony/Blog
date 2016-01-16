package com.slightlyloony.blog.handlers.cookies;

/**
 * Represents a single request cookie.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RequestCookie {

    private String name;
    private String value;


    public static RequestCookie parse( final String _value ) {

        // sanity check...
        if( _value == null )
            return null;

        // get our name and value, and bail out if we don't have both...
        String[] parts = _value.split( "=", 2 );
        if( parts.length != 2 )
            return null;

        // all is ok; make our result...
        RequestCookie result = new RequestCookie();
        result.name = parts[0].trim();
        result.value = parts[1].trim();

        return result;
    }


    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }
}
