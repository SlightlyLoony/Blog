package com.slightlyloony.blog.templates.functions;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.templates.sources.data.Datum;

import java.text.MessageFormat;

/**
 * Generic template function container for all functions, supplying argument checking and interfacing.  The actual computation bit of each template
 * function is defined in {@link FunctionDef}.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Function implements Datum {

    private FunctionDef def;
    private Datum[] args;


    private Function( final FunctionDef _def, final Datum[] _args ) {
        def = _def;
        args = _args;
    }


    /**
     * Creates a new instance of this class, or returns null if there was a problem.  See {@link #validate(FunctionDef, Datum[])} to get the details
     * of why there was a problem.
     *
     * @param _def the function definition for the new instance
     * @param _args the arguments for the new instance
     */
    public static Function create( final FunctionDef _def, final Datum[] _args ) {

        return (validate( _def, _args ) == null ) ? new Function( _def, _args ) : null;
    }


    /**
     * Checks the given function definition and arguments for validity.  Returns null if there are no errors, or an explanatory string if there are.
     *
     * @param _def the function definition to be checked
     * @param _args the arguments to be checked
     * @return null if there are no errors, or an explanatory string if there are
     */
    public static String  validate( final FunctionDef _def, final Datum[] _args ) {

        if( (_def == null) || (_args == null) )
            throw new HandlerIllegalArgumentException( "Missing function definition or argument" );

        if( _args.length < _def.getMinArgs() )
            return MessageFormat.format( "Function {0} requires at least {1} argument(s), but there are only {2}",
                    _def.name(), _def.getMinArgs(), _args.length );

        if( (_def.getMaxArgs() >= 0) && (_args.length > _def.getMaxArgs() ) )
            return MessageFormat.format( "Function {1} may not have more than {1} arguments, but there are {2}",
                    _def.name(), _def.getMinArgs(), _args.length );

        return null;
    }


    /**
     * Returns the value of this datum.
     *
     * @return a getter for the value of this datum
     */
    @Override
    public Object getValue() {
        return def.action( args ).getValue();
    }
}
