package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.StringDatum;
import com.slightlyloony.blog.users.User;

/**
 * Represents the hierarchical path to a datum from a root source.  Paths are always created with a dotted-form string path (like "user.firstname")
 * but are lazily resolved at runtime to integer indices.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Path {

    private final String[] names;
    private final Integer[] indices;


    private Path( final String[] _names ) {
        names = _names;
        indices = new Integer[_names.length];
    }


    /**
     * Creates a new instance of this class from the given dotted-form path, or returns null if there was a problem with the path.  See the
     * {@link #validate(String)} method for how to get information about why the path failed.
     *
     * @param _path the string containing a dotted-form path
     * @return a new instance of this class created from the given path, or null if the path was invalid
     */
    public static Path create( final String _path ) {

        // make sure we have a good path...
        return (validate( _path ) != null ) ? null : new Path( _path.split( "\\." ) );
    }


    /**
     * Returns the datum at the given path.  If any of the path parts evaluate incorrectly, then an error message is returned as a string datum.
     * Otherwise, the datum identified by the last part of the path is returned.
     *
     * @param _source the source at the root of the path
     * @param _user the user whose authorities and name determine whether this datum may be accessed
     * @return the datum desired, or an explanatory string datum if there was a problem
     */
    public Datum getDatum( final Source _source, final User _user ) {

        Datum value = _source;
        Source source = null;

        for( int i = 0; i < names.length; i++ ) {

            if( !(value instanceof Source) )
                return new StringDatum( "Path name '" + names[i-1] + "' is not a source" );

            source = (Source) value;

            // if we haven't already done so, resolve this name to an index...
            if( indices[i] == null ) {
                indices[i] = source.getDefs().byName( names[i] );
                if( indices[i] == null )
                    return new StringDatum( "Path name '" + names[i] + "' is undefined" );
            }

            Datum newValue = source.get( _user, indices[i] );

            if( newValue == null )
                return new StringDatum( "Value was null" );

            value = newValue;
        }
        return value;
    }


    public static String validate( final String _path ) {

        if( _path == null )
            return "The path is null.";

        if( _path.length() == 0 )
            return null;

        boolean dotLast = false;
        int partChars = 0;
        char lastChar = 0;
        for( int i = 0; i < _path.length(); i++ ) {

            char c = _path.charAt( i );
            if( c == '.' ) {
                if( dotLast )
                    return "The path contains two dots in a row: " + _path;
                dotLast = true;
                if( (i != 0) && !isValidEnd( lastChar ) )
                    return "The character at position " + i + " is not valid alphabetic or numeric ('" + lastChar + "'): " + _path;
                partChars = 0;
            }
            else {
                dotLast = false;
                if( (partChars == 0) && !isAlphabetic( c ) )
                    return "The path character at position " + (i+1) + " is not alphabetic ('" + c + "'): " + _path;
                if( !isValidCenter( c ) )
                    return "The path character at position " + (i+1) + " is not alphabetic, numeric, or underscore ('" + c + "'): " + _path;
                partChars++;
            }
            lastChar = c;
        }
        if( !isValidEnd( lastChar ) )
            return "The last character of the path is not valid alphabetic or numeric ('" + lastChar + "'): " + _path;
        return null;
    }


    private static boolean isAlphabetic( final char _c ) {
        return ((_c >= 'a') && (_c <= 'z')) || ((_c >= 'A') && (_c <= 'Z'));
    }


    private static boolean isNumeric( final char _c ) {
        return (_c >= '0') && (_c <= '9');
    }


    private static boolean isValidCenter( final char _c ) {
        return isAlphabetic( _c ) || isNumeric( _c ) || (_c == '_');
    }

    private static boolean isValidEnd( final char _c ) {
        return isAlphabetic( _c ) || isNumeric( _c );
    }
}
