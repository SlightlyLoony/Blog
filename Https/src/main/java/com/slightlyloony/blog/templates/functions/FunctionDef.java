package com.slightlyloony.blog.templates.functions;

import com.slightlyloony.blog.templates.sources.data.Datum;

import java.util.BitSet;

import static com.slightlyloony.blog.templates.TemplateUtil.*;

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


    public Object action( final Datum... _args ) {
        return functionAction.action( _args );
    }


    private static FunctionAction getAndAction() { return _args -> {
            boolean result = true;
            for( Datum datum : _args ) {
                result &= toBool( datum );
            }
            return result;
        };
    }


    private static FunctionAction getOrAction() { return _args -> {
            boolean result = false;
            for( Datum datum : _args ) {
                result |= toBool( datum );
            }
            return result;
        };
    }


    private static FunctionAction getXorAction() { return _args -> {
            boolean result = false;
            for( Datum datum : _args ) {
                result ^= toBool( datum );
            }
            return result;
        };
    }


    private static FunctionAction getNotAction() {
        return _args -> !toBool( _args[0] );
    }


    private static FunctionAction getAddAction() { return _args -> {
            int sum = 0;
            for( Datum datum : _args ) {
                sum += toInt( datum );
            }
            return sum;
        };
    }


    private static FunctionAction getSubAction() {
        return _args -> toInt( _args[0]) - toInt( _args[1] );
    }


    private static FunctionAction getMulAction() {
        return _args -> toInt( _args[0]) * toInt( _args[1] );
    }


    private static FunctionAction getDivAction() {
        return _args -> toInt( _args[0]) / toInt( _args[1] );
    }


    private static FunctionAction getModAction() {
        return _args -> toInt( _args[0]) % toInt( _args[1] );
    }


    private static FunctionAction getOddAction() {
        return _args -> 1 == (toInt( _args[0]) % 2);
    }


    private static FunctionAction getEvenAction() {
        return _args -> 0 == (toInt( _args[0]) % 2);
    }


    private static FunctionAction getEqAction() {
        return _args -> compare( _args[0], _args[1] ) == 0;
    }


    private static FunctionAction getNeqAction() {
        return _args -> compare( _args[0], _args[1] ) != 0;
    }


    private static FunctionAction getGtAction() {
        return _args -> compare( _args[0], _args[1] ) > 0;
    }


    private static FunctionAction getGteAction() {
        return _args -> compare( _args[0], _args[1] ) >= 0;
    }


    private static FunctionAction getLtAction() {
        return _args -> compare( _args[0], _args[1] ) < 0;
    }


    private static FunctionAction getLteAction() {
        return _args -> compare( _args[0], _args[1] ) <= 0;
    }


    private static FunctionAction getConcatAction() { return _args -> {
            StringBuilder sb = new StringBuilder();
            for( Datum datum : _args ) {
                sb.append( toStr( datum ) );
            }
            return sb.toString();
        };
    }


    private static FunctionAction getLowerAction() {
        return _args -> toStr( _args[0] ).toLowerCase();
    }


    private static FunctionAction getUpperAction() {
        return _args -> toStr( _args[0] ).toUpperCase();
    }


    private static FunctionAction getLeftAction() { return _args -> {
            int len = toInt( _args[1] );
            String str = toStr( _args[0] );
            return (len <= 0) ? "" : str.substring( 0, len );
        };
    }


    private static FunctionAction getRightAction() { return _args -> {
            int len = toInt( _args[1] );
            String str = toStr( _args[0] );
            return (len <= 0) ? "" : str.substring( Math.max( 0, str.length() - len ), str.length() );
        };
    }


    private static FunctionAction getMidAction() { return _args -> {
            int pos = toInt( _args[1] );
            int len = toInt( _args[2] );
            String str = toStr( _args[0] );
            int strl = str.length();
            int start = (pos >= 0) ? Math.min( strl, pos ) : Math.max( 0, strl + pos );
            int end = Math.min( strl, start + len );
            return (len <= 0) ? "" : str.substring( start, end );
        };
    }


    private static FunctionAction getSubstringAction() { return _args -> {
            int spos = toInt( _args[1] );
            int epos = toInt( _args[2] );
            String str = toStr( _args[0] );
            int strl = str.length();
            int start = (spos >= 0) ? Math.min( strl, spos ) : Math.max( 0, strl + spos );
            int end = (epos >= 0) ? Math.min( strl, epos ) : Math.max( 0, strl + epos);
            end = Math.max( end, start );
            return str.substring( start, end );
        };
    }


    private static FunctionAction getLengthAction() {
        return _args -> toStr( _args[0] ).length();
    }


    private static FunctionAction getIndexOfAction() {
        return _args -> toStr( _args[0] ).indexOf( toStr( _args[1] ) );
    }


    private static FunctionAction getTrimAction() {
        return _args -> toStr( _args[0] ).trim();
    }


    private static FunctionAction getEscHTMLAction() { return _args -> {

            StringBuilder sb = new StringBuilder();

            for( char c : toStr( _args[0] ).toCharArray() ) {

                if( HTML_ESCAPEES.get( c ) ) {
                    sb.append( "&#" );
                    sb.append( (int) c );
                    sb.append( ';' );
                }
                else
                    sb.append( c );
            }
            return sb.toString();
        };
    }


    private static FunctionAction getHasAction() { return _args -> {
            Object argVal = _args[0].getValue();
            return (argVal != null) && (!"".equals( argVal.toString() ));
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

        // get the value of both our datums...
        Object a = _a.getValue();
        Object b = _b.getValue();

        // now compare them as the type of the first value...
        if( a instanceof Integer )
            return ((Integer) a).compareTo( toInt( b ) );
        else if( a instanceof Boolean )
            return ((Boolean) a).compareTo( toBool( b ) );
        return a.toString().compareTo( b.toString() );
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
