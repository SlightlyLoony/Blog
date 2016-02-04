package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.templates.sources.data.BooleanDatum;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.IntegerDatum;
import com.slightlyloony.blog.templates.sources.data.StringDatum;

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
    public static String toStr( final Object _value ) {

        if( _value == null )
            return "";

        Object val = (_value instanceof Datum) ? ((Datum) _value).getValue() : _value;

        if( val == null )
            return "";

        if( val instanceof String )
            return (String) val;

        return val.toString();
    }


    public static boolean toBool( final Object _value ) {

        if( _value == null )
            return false;

        Object val = (_value instanceof Datum) ? ((Datum) _value).getValue() : _value;

        if( val == null )
            return false;

        if( val instanceof Boolean )
            return (Boolean) val;

        if( val instanceof Integer )
            return (Integer) val != 0;

        String str = (val instanceof String) ? (String) val  : val.toString();

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


    public static int toInt( final Object _value ) {

        if( _value == null )
            return 0;

        Object val = (_value instanceof Datum) ? ((Datum) _value).getValue() : _value;

        if( val == null )
            return 0;

        if( val instanceof Integer )
            return (Integer) val;

        if( val instanceof Boolean )
            return ((Boolean) val) ? 1 : 0;

        String str = (val instanceof String) ? (String) val  : val.toString();
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


    public static Datum toDatum( final Object _arg ) {

        if( _arg == null )
            return new StringDatum( "" );

        Object arg = ( _arg instanceof Datum ) ? ((Datum) _arg).getValue() : _arg;

        if( arg == null )
            return new StringDatum( "" );

        if( arg instanceof String )
            return new StringDatum( (String) arg );

        if( arg instanceof Integer )
            return new IntegerDatum( (Integer) arg );

        if( arg instanceof Boolean )
            return new BooleanDatum( (Boolean) arg );

        return new StringDatum( arg.toString() );
    }


    private TemplateUtil() {
        // prevent instantiation...
    }
}
