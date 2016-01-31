package com.slightlyloony.blog.templates.functions;

import com.slightlyloony.blog.templates.sources.data.BooleanDatum;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.IntegerDatum;
import com.slightlyloony.blog.templates.sources.data.StringDatum;

import java.util.BitSet;

import static com.slightlyloony.blog.templates.TemplateUtil.toBool;
import static com.slightlyloony.blog.templates.TemplateUtil.toInt;
import static com.slightlyloony.blog.templates.TemplateUtil.toStr;

/**
 * Defines all template functions in the system.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum FunctionDef implements FunctionAction {

    lower     ( 1,  1, getLowerAction()     ),
    upper     ( 1,  1, getUpperAction()     ),
    left      ( 2,  2, getLeftAction()      ),
    right     ( 2,  2, getRightAction()     ),
    mid       ( 3,  3, getMidAction()       ),
    substring ( 3,  3, getSubstringAction() ),
    length    ( 1,  1, getLengthAction()    ),
    indexOf   ( 2,  2, getIndexOfAction()   ),
    trim      ( 1,  1, getTrimAction()      ),
    concat    ( 0, -1, getConcatAction()    ),
    escHTML   ( 1,  1, getEscHTMLAction()   ),
    eq        ( 2,  2, getEqAction()        ),
    neq       ( 2,  2, getNeqAction()       ),
    gt        ( 2,  2, getGtAction()        ),
    gte       ( 2,  2, getGteAction()       ),
    lt        ( 2,  2, getLtAction()        ),
    lte       ( 2,  2, getLteAction()       ),
    has       ( 1,  1, getHasAction()       ),
    add       ( 1, -1, getAddAction()       ),
    sub       ( 2,  2, getSubAction()       ),
    mul       ( 2,  2, getMulAction()       ),
    div       ( 2,  2, getDivAction()       ),
    mod       ( 2,  2, getModAction()       ),
    odd       ( 1,  1, getOddAction()       ),
    even      ( 1,  1, getEvenAction()      ),
    and       ( 1, -1, getAndAction()       ),
    or        ( 1, -1, getOrAction()        ),
    xor       ( 1, -1, getXorAction()       ),
    not       ( 1,  1, getNotAction()       );


    private static BitSet HTML_ESCAPEES = getEscapees();


    private int minArgs;
    private int maxArgs;
    private FunctionAction functionAction;


    FunctionDef( final int _minArgs, final int _maxArgs, final FunctionAction _action ) {
        minArgs = _minArgs;
        maxArgs = _maxArgs;
        functionAction = _action;
    }


    public int getMinArgs() {
        return minArgs;
    }


    public int getMaxArgs() {
        return maxArgs;
    }


    public Datum action( final Datum... _arguments ) {
        return functionAction.action( _arguments );
    }


    public FunctionAction getAction() {
        return functionAction;
    }


    private static FunctionAction getAndAction() { return _arguments -> {
            boolean result = true;
            for( Datum datum : _arguments ) {
                result &= toBool( datum.getValue() );
            }
            return new BooleanDatum( result );
        };
    }


    private static FunctionAction getOrAction() { return _arguments -> {
            boolean result = false;
            for( Datum datum : _arguments ) {
                result |= toBool( datum.getValue() );
            }
            return new BooleanDatum( result );
        };
    }


    private static FunctionAction getXorAction() { return _arguments -> {
            boolean result = false;
            for( Datum datum : _arguments ) {
                result ^= toBool( datum.getValue() );
            }
            return new BooleanDatum( result );
        };
    }


    private static FunctionAction getAddAction() { return _arguments -> {
            int sum = 0;
            for( Datum datum : _arguments ) {
                sum += toInt( datum.getValue() );
            }
            return new IntegerDatum( sum );
        };
    }


    private static FunctionAction getNotAction() { return _arguments -> {
            return new BooleanDatum( !toBool( _arguments[0] ) );
        };
    }


    private static FunctionAction getSubAction() { return _arguments -> {
            return new IntegerDatum( toInt( _arguments[0 ]) - toInt( _arguments[1] ) );
        };
    }


    private static FunctionAction getMulAction() { return _arguments -> {
            return new IntegerDatum( toInt( _arguments[0 ]) * toInt( _arguments[1] ) );
        };
    }


    private static FunctionAction getDivAction() { return _arguments -> {
            return new IntegerDatum( toInt( _arguments[0 ]) / toInt( _arguments[1] ) );
        };
    }


    private static FunctionAction getModAction() { return _arguments -> {
            return new IntegerDatum( toInt( _arguments[0 ]) % toInt( _arguments[1] ) );
        };
    }


    private static FunctionAction getOddAction() { return _arguments -> {
            return new BooleanDatum( 1 == (toInt( _arguments[0 ]) % 2) );
        };
    }


    private static FunctionAction getEvenAction() { return _arguments -> {
            return new BooleanDatum( 0 == (toInt( _arguments[0 ]) % 2) );
        };
    }


    private static FunctionAction getEqAction() { return _arguments -> {
            return new BooleanDatum( compare( _arguments[0], _arguments[1] ) == 0 );
        };
    }


    private static FunctionAction getNeqAction() { return _arguments -> {
            return new BooleanDatum( compare( _arguments[0], _arguments[1] ) != 0 );
        };
    }


    private static FunctionAction getGtAction() { return _arguments -> {
            return new BooleanDatum( compare( _arguments[0], _arguments[1] ) > 0 );
        };
    }


    private static FunctionAction getGteAction() { return _arguments -> {
            return new BooleanDatum( compare( _arguments[0], _arguments[1] ) >= 0 );
        };
    }


    private static FunctionAction getLtAction() { return _arguments -> {
            return new BooleanDatum( compare( _arguments[0], _arguments[1] ) < 0 );
        };
    }


    private static FunctionAction getLteAction() { return _arguments -> {
            return new BooleanDatum( compare( _arguments[0], _arguments[1] ) <= 0 );
        };
    }


    private static FunctionAction getConcatAction() { return _arguments -> {
            StringBuilder sb = new StringBuilder();
            for( Datum datum : _arguments ) {
                sb.append( toStr( datum.getValue() ) );
            }
            return new StringDatum( sb.toString() );
        };
    }


    private static FunctionAction getLowerAction() { return _arguments -> {
            return new StringDatum( toStr( _arguments[0] ).toLowerCase() );
        };
    }


    private static FunctionAction getUpperAction() { return _arguments -> {
            return new StringDatum( toStr( _arguments[0] ).toUpperCase() );
        };
    }


    private static FunctionAction getLeftAction() { return _arguments -> {
            int len = toInt( _arguments[1] );
            String str = toStr( _arguments[0] );
            return new StringDatum( (len <= 0) ? "" : str.substring( 0, len ) );
        };
    }


    private static FunctionAction getRightAction() { return _arguments -> {
            int len = toInt( _arguments[1] );
            String str = toStr( _arguments[0] );
            return new StringDatum( (len <= 0) ? "" : str.substring( Math.max( 0, str.length() - len ), str.length() ) );
        };
    }


    private static FunctionAction getMidAction() { return _arguments -> {
            int pos = toInt( _arguments[1] );
            int len = toInt( _arguments[2] );
            String str = toStr( _arguments[0] );
            int strl = str.length();
            int start = (pos >= 0) ? Math.min( strl, pos ) : Math.max( 0, strl + pos );
            int end = Math.min( strl, start + len );
            return new StringDatum( (len <= 0) ? "" : str.substring( start, end ) );
        };
    }


    private static FunctionAction getSubstringAction() { return _arguments -> {
            int spos = toInt( _arguments[1] );
            int epos = toInt( _arguments[2] );
            String str = toStr( _arguments[0] );
            int strl = str.length();
            int start = (spos >= 0) ? Math.min( strl, spos ) : Math.max( 0, strl + spos );
            int end = (epos >= 0) ? Math.min( strl, epos ) : Math.max( 0, strl + epos);
            end = Math.max( end, start );
            return new StringDatum( str.substring( start, end ) );
        };
    }


    private static FunctionAction getLengthAction() { return _arguments -> {
            return new IntegerDatum( toStr( _arguments[0] ).length() );
        };
    }


    private static FunctionAction getIndexOfAction() { return _arguments -> {
            return new IntegerDatum( toStr( _arguments[0] ).indexOf( toStr( _arguments[1] ) ) );
        };
    }


    private static FunctionAction getTrimAction() { return _arguments -> {
            return new StringDatum( toStr( _arguments[0] ).trim() );
        };
    }


    private static FunctionAction getEscHTMLAction() { return _arguments -> {

            StringBuilder sb = new StringBuilder();

            for( char c : toStr( _arguments[0] ).toCharArray() ) {

                if( HTML_ESCAPEES.get( c ) ) {
                    sb.append( "&#" );
                    sb.append( (int) c );
                    sb.append( ';' );
                }
                else
                    sb.append( c );
            }
            return new StringDatum( sb.toString() );
        };
    }


    private static FunctionAction getHasAction() { return _arguments -> {
            Object argVal = _arguments[0].getValue();
            return new BooleanDatum( (argVal != null) && (!"".equals( argVal.toString() )) );
        };
    }


    /**
     * Returns -1, 0, or 1 as the first argument is less than, equal to, or greater than the second argument.
     *
     * @param _a the first argument
     * @param _b the second argument
     * @return -1, 0, or 1 as the first argument is less than, equal to, or greater than the second argument
     */
    private static int compare( final Datum _a, final Datum _b ) {

        if( _a instanceof IntegerDatum ) {
            Integer a = toInt( _a );
            Integer b = toInt( _b );
            return a.compareTo( b );
        }
        else if( _a instanceof BooleanDatum ) {
            Boolean a = toBool( _a );
            Boolean b = toBool( _b );
            return a.compareTo( b );
        }
        String a = toStr( _a );
        String b = toStr( _b );
        return a.compareTo( b );
    }


    /**
     * Returns a {@link BitSet} indexed by character code, with bits true if the corresponding character needs escaping for HTML.
     *
     * @return a bit set with bits true if the corresponding character needs escaping for HTML
     */
    private static BitSet getEscapees() {

        BitSet result = new BitSet();
        result.set( '\t' );
        result.set( '\r' );
        result.set( '\n' );
        result.set( '\\' );
        result.set( '"'  );
        result.set( '\'' );
        result.set( '<'  );
        result.set( '>'  );
        result.set( '&'  );
        return result;
    }
}
