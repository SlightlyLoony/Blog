package com.slightlyloony.blog.util;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;

import java.util.Arrays;

/**
 * Static container class for utilities related to IDs.  These use base64url encoding as defined by RFC 4648.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ID {

    private static final byte[] CHAR_TO_VALUE = getCharToValue();
    private static final char[] VALUE_TO_CHAR = getValueToChar();


    /**
     * Returns the URL Base32 encoding of the least significant 50 bits of the given value.  The returned value is always a 10 character string.
     *
     * @param _idNum the 50 bit unsigned value to encode.
     * @return the encoded equivalent of the given number.
     */
    public static String encode( final long _idNum ) {

        char[] chars = new char[10];
        long id = Long.rotateLeft( _idNum, 14 );  // gets the most significant 5 bits of _idNum as the most significant 5 bits of id...

        for( int i = 0; i < 10; i++ ) {
            id = Long.rotateLeft( id, 5 );
            chars[i] = VALUE_TO_CHAR[(int)(id & 0x1f)];
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
            throw new HandlerIllegalArgumentException( "Invalid ID: " + _id );

        long result = 0;

        for( int i = 0; i < 10; i++ ) {
            result <<= 5;
            int val = get( _id.charAt( i ) );
            if( val < 0 )
                throw new HandlerIllegalArgumentException( "Invalid character in ID: " + _id.charAt( i ) );
            result += val;
        }

        return result;
    }


    /**
     * Returns -1, 0, or +1 as the given base64url character _a is less than, equal to, or greater than the given base32 character _b, using the
     * base64url collation sequence.  Throws {@link IllegalArgumentException} if either character is invalid for base32.
     *
     * @param _a the first comparand
     * @param _b the second comparand
     * @return the value -1, 0, or +1 as the given base64url character _a is less than, equal to, or greater than the given base64url character _b
     */
    public static int compare( final char _a, final char _b ) {

        if( !isValid( _a )|| !isValid( _b ))
            throw new HandlerIllegalArgumentException( "Invalid value: " + _a + " or " + _b );

        return Integer.signum( get( _a ) - get( _b ));
    }


    /**
     * Returns -1, 0, or +1 as the given base32 _a is less than, equal to, or greater than the given base32 _b.  Throws
     * {@link IllegalArgumentException} if either given string is null, if the given strings aren't of equal length, or if either string contains
     * invalid base32 characters.
     *
     * @param _a the first comparand
     * @param _b the second comparand
     * @return the value -1, 0, or +1 as the given base64url _a is less than, equal to, or greater than the given base64url _b
     */
    public static int compare( final String _a, final String _b ) {

        if( (_a == null) || (_b == null) || (_a.length() != _b.length()) )
            throw new HandlerIllegalArgumentException( "Invalid value: " + _a + " or " + _b );

        for( int i = 0; i < _a.length(); i++ ) {
            int cc = compare( _a.charAt( i ), _b.charAt( i ) );
            if( cc != 0 )
                return cc;
        }
        return 0;
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
        byte[] result = new byte[256];
        Arrays.fill( result, (byte) -1 );

        // fill in our valid values...
        for( int i = 'A'; i <= 'Z'; i++ )
            result[i] = (byte) (i - 'A');
        for( int i = '0'; i <= '5'; i++ )
            result[i] = (byte) (26 + i - '0' );
        return result;
    }


    private static char[] getValueToChar() {

        // make our array of characters...
        char[] result = new char[32];
        for( int i = 0; i < 26; i++ )
            result[i]    = (char)('A' + i);
        for( int i = 0; i < 6; i++ )
            result[i+26] = (char)('0' + i);
        return result;
    }


    private ID() {
        // just here to prevent instantiation...
    }
}
