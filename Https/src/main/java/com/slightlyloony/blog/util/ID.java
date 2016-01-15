package com.slightlyloony.blog.util;

import com.google.common.io.BaseEncoding;

import java.util.Arrays;

/**
 * Static container class for utilities related to IDs.  These use base64url encoding as defined by RFC 4648.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ID {

    private static final BaseEncoding BASE64 = BaseEncoding.base64Url();
    private static final byte[] CHAR_TO_VALUE = getCharToValue();
    private static final char[] VALUE_TO_CHAR = getValueToChar();


    /**
     * Returns the URL Base64 encoding of the least significant 60 bits of the given value.  The returned value is always a 10 character string.
     *
     * @param _idNum the 60 bit unsigned value to encode.
     * @return the encoded equivalent of the given number.
     */
    public static String encode( final long _idNum ) {

        char[] chars = new char[10];
        long id = Long.rotateLeft( _idNum, 2 );  // gets the most significant 6 bits of _idNum as the most significant 6 bits of id...

        for( int i = 0; i < 10; i++ ) {
            id = Long.rotateLeft( id, 6 );
            chars[i] = VALUE_TO_CHAR[(int)(id & 0x3f)];
        }
        return new String( chars );
    }


    /**
     * Decodes the given ID string to a long.  Note that the given string <i>must</i> be exactly 10 characters long.
     *
     * @param _id the ID to decode.
     * @return the long value of the given ID, decoded.
     */
    public static long decode( final String _id ) {

        // make sure we didn't get something crazy...
        if( (_id == null) || (_id.length() != 10) )
            throw new IllegalArgumentException( "Invalid ID: " + _id );

        long result = 0;

        for( int i = 0; i < 10; i++ ) {
            result <<= 6;
            int val = get( _id.charAt( i ) );
            if( val < 0 )
                throw new IllegalArgumentException( "Invalid character in ID: " + _id.charAt( i ) );
            result += val;
        }

        return result;
    }


    /**
     * Returns the numeric value of the given character, or -1 if the character is invalid.
     *
     * @param _char the character to get a numeric value for
     * @return the numeric value of the given character, or -1 if the character is invalid
     */
    public static int get( final char _char ) {
        return isValid( _char ) ? CHAR_TO_VALUE[_char] : -1;
    }


    public static char get( final int _value ) {
        return VALUE_TO_CHAR[_value & 0x3f];
    }


    public static boolean isValid( final char _char ) {
        return (_char < 128) && (CHAR_TO_VALUE[_char] >= 0);
    }


    public static boolean isValid( final CharSequence _sequence ) {

        if( (_sequence == null) || (_sequence.length() == 0))
            return false;

        for( int i = 0; i < _sequence.length(); i++ ) {
            if( !isValid( _sequence.charAt( i ) ) )
                return false;
        }
        return true;
    }


    private static byte[] getCharToValue() {

        // get our default values
        byte[] result = new byte[128];
        Arrays.fill( result, (byte) -1 );

        // fill in our valid values...
        for( int i = 'A'; i <= 'Z'; i++ )
            result[i] = (byte) (i - 'A');
        for( int i = 'a'; i <= 'z'; i++ )
            result[i] = (byte) (26 + i - 'a' );
        for( int i = '0'; i <= '9'; i++ )
            result[i] = (byte) (52 + i - '0' );
        result['-'] = 62;
        result['_'] = 63;

        return result;
    }


    private static char[] getValueToChar() {

        // make our array of characters...
        char[] result = new char[64];
        for( int i = 0; i < 26; i++ ) {
            result[i]    = (char)('A' + i);
            result[i+26] = (char)('a' + i);
        }
        for( int i = 0; i < 10; i++ )
            result[i+52] = (char)('0' + i);
        result[62] = '-';
        result[63] = '_';

        return result;
    }


    private ID() {
        // just here to prevent instantiation...
    }
}
