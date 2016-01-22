package com.slightlyloony.blog.util;

/**
 * Static container class for string utilities.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class S {


    public static byte[] toUTF8( final String _s ) {
        return (_s == null) ? null : _s.getBytes( Constants.UTF8 );
    }


    public static String fromUTF8( final byte[] _utf8 ) {
        return (_utf8 == null) ? null : new String( _utf8, Constants.UTF8 );
    }


    private S() {
        // prevent instantiation...
    }
}
