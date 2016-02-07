package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.TemplateRenderingContext;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.StringDatum;

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
     * @return the datum desired, or an explanatory string datum if there was a problem
     */
    public Datum getDatum() {

        Datum value = TemplateRenderingContext.get().getSource();
        Source source;

        for( int i = 0; i < names.length; i++ ) {

            if( !(value instanceof Source) )
                return new StringDatum( "{{Path name '" + names[i-1] + "' is not a source}}" );

            source = (Source) value;

            // if we haven't already done so, resolve this name to an index...
            if( indices[i] == null ) {
                indices[i] = source.getDefs().byName( names[i] );
                if( indices[i] == null ) {

                    // make a special, hacky check to see if we're resolving a built-in variable on a list source; if so, hack it in...
                    if( source instanceof ListSource ) {
                        Datum lister = ((ListSource) source).resolveSpecialVariables( names[i] );
                        if( lister != null )
                            return lister;
                    }

                    // otherwise, we've got an undefined element...
                    return new StringDatum( "{{Path name '" + names[i] + "' is undefined}}" );
                }
            }

            Datum newValue = source.get( indices[i] );

            if( newValue == null )
                return new StringDatum( "{{Value was null}}" );

            value = newValue;
        }
        return value;
    }


    /**
     * Sets the value at the given path to the given datum, creating the datum if necessary.  This operation is only valid for the case in which
     * the last source (the next-to-last path element) is a {@link VariableSource}.  Any attempt to set a value on any other type of {@link Source}
     * will result in an error indication and no change to the value.
     *
     * @param _datum the value to set this path's datum to
     * @return true if the invocation successfully set the value
     */
    public boolean setDatum( final Datum _datum ) {

        if( _datum == null )
            return false;

        Datum value = TemplateRenderingContext.get().getSource();
        Source source;

        // first we get the last source...
        int i;
        for( i = 0; i < names.length - 1; i++ ) {

            if( !(value instanceof Source) )
                return false;

            source = (Source) value;

            // if we haven't already done so, resolve this name to an index...
            if( indices[i] == null ) {
                indices[i] = source.getDefs().byName( names[i] );
                if( indices[i] == null )
                    return false;
            }

            Datum newValue = source.get( indices[i] );

            if( newValue == null )
                return false;

            value = newValue;
        }

        // if we don't have a VariableSource here, we've got problems...
        if( !(value instanceof VariableSource) )
            return false;

        VariableSource variableSource = (VariableSource) value;

        // if this particular variable doesn't already exist, we need to create it...
        if( variableSource.getDefs().byName( names[i] ) == null )
            variableSource.create( names[i] );

        // now resolve the index, if that hasn't already been done...
        if( indices[i] == null ) {
            indices[i] = variableSource.getDefs().byName( names[i] );
            if( indices[i] == null )
                return false;
        }

        // we thought we'd never get here, but ... now we can actually set the value!
        variableSource.set( indices[i], _datum );
        return true;
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for( String name : names ) {
            if( sb.length() != 0 )
                sb.append( '.' );
            sb.append( name );
        }
        return sb.toString();
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
