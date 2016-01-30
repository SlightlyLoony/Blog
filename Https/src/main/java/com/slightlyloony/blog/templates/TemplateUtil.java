package com.slightlyloony.blog.templates;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A static container class for utlity functions used by templates.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TemplateUtil {


    /**
     * Converts the given value to a string, following the rules for templates (see TemplateLanguage.txt for details).
     *
     * @param _value the value (of arbitrary type) to convert
     * @return the converted string
     */
    public static String toString( final Object _value ) {

        if( _value == null )
            return "";

        if( _value instanceof String )
            return (String) _value;

        return _value.toString();
    }


    public static boolean toBoolean( final Object _value ) {

        if( _value == null )
            return false;

        if( _value instanceof Boolean )
            return (Boolean) _value;

        if( _value instanceof Integer )
            return (Integer) _value != 0;

        String str = (_value instanceof String) ? (String) _value  : _value.toString();

        switch( str ) {
            case "":
            case "F":
            case "f":
            case "0":
            case "false":
            case "FALSE":
                return false;
            default:
                return true;
        }

    }


    public static int toInteger( final Object _value ) {

        if( _value == null )
            return 0;

        if( _value instanceof Integer )
            return (Integer) _value;

        if( _value instanceof Boolean )
            return ((Boolean) _value) ? 1 : 0;

        String str = (_value instanceof String) ? (String) _value  : _value.toString();
        if( str.length() == 0 )
            return 0;

        // see if we have any integer value at all...
        if( Character.isDigit( str.charAt( 0 ) ) ||
                ((str.charAt(0) == '0') && (str.length() > 1) && (Character.isDigit( str.charAt( 1 ) ) ) ) ) {

            try {
                NumberFormat nf = NumberFormat.getIntegerInstance();
                return (Integer) nf.parse( str );
            }
            catch( ParseException e ) {
                // because we checked for numberness, this should never happen...
                return 0;
            }
        }
        return 0;
    }


    private TemplateUtil() {
        // prevent instantiation...
    }
}
