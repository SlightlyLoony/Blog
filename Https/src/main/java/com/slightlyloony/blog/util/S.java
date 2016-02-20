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


    /**
     * Returns the length of the given string, in bytes.  Note that this implementation depends on the string being stored as UTF-16 with no
     * special treatment (in {@link String#length()}) of Unicode characters that take more than 16 bits.
     *
     * @param _str the string to get the length of
     * @return the length of the given string in bytes
     */
    public static int strByteSize( final String _str ) {
        return (_str == null) ? 0 : 2 * _str.length();
    }


    private S() {
        // prevent instantiation...
    }
}
