package com.slightlyloony.blog.templates.functions;

import com.slightlyloony.blog.templates.TemplateUtil;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.IntegerDatum;
import com.slightlyloony.blog.templates.sources.data.StringDatum;

/**
 * Defines all template functions in the system.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum FunctionDef implements FunctionAction {

    concat( 0, -1, getConcatAction() ),
    add   ( 1, -1, getAddAction()    );


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


    private static FunctionAction getAddAction() { return _arguments -> {
            int sum = 0;
            for( Datum datum : _arguments ) {
                sum += TemplateUtil.toInt( datum.getValue() );
            }
            return new IntegerDatum( sum );
        };
    }


    private static FunctionAction getConcatAction() { return _arguments -> {
            StringBuilder sb = new StringBuilder();
            for( Datum datum : _arguments ) {
                sb.append( TemplateUtil.toStr( datum.getValue() ) );
            }
            return new StringDatum( sb.toString() );
        };
    }
}
