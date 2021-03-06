package com.slightlyloony.blog.templates.sources.data;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.templates.sources.Source;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Encapsulates the definition of a datum, including a factory to produce the actual datum given a source.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DatumDef {

    private static final Logger LOG = LogManager.getLogger();

    private final String name;
    private final Class<? extends Datum> klass;
    private final ValueGetter getter;
    private final Authorizor authorizor;


    public DatumDef( final String _name, final Class<? extends Datum> _class, final ValueGetter _getter, final Authorizor _authorizor ) {

        if( (_class == null) || (_name == null) || (_getter == null) || (_authorizor == null) )
            throw new HandlerIllegalArgumentException( "Missing a required argument" );

        name = _name;
        klass = _class;
        getter = _getter;
        authorizor = _authorizor;
    }


    public DatumDef( final String _name, final Class<? extends Datum> _class, final ValueGetter _getter ) {
        this( _name, _class, _getter, (_source) -> true );
    }


    /**
     * This constructor exists only to support variable sources.  It will create a data definition from a datum that is to be assigned to a variable.
     * The value getter will be null, because the datum will be instantiated by the variable source, not the usual mechanism in SourceBase.
     *
     * @param _name the name of this datum
     */
    public DatumDef( final String _name ) {

        if( _name == null )
            throw new HandlerIllegalArgumentException( "Missing required name argument" );

        name = _name;
        klass = null;
        getter = _source -> null;
        authorizor = (_source ) -> true;
    }


    public Datum getDatum( final Source _source ) {

        if( _source == null )
            throw new HandlerIllegalArgumentException( "Missing required source argument" );

        Datum result = null;
        try {

            // we don't know the type of the value parameter, so we'll just try the one and only constructor this class should have...
            Constructor[] ctors = klass.getConstructors();
            if( ctors.length > 1 ) {
                String msg = "Class has other than ZERO or ONE constructor: " + klass.getSimpleName();
                LOG.error( msg );
                throw new HandlerIllegalStateException( msg );
            }

            // then we'll invoke that constructor to get our Datum...
            Constructor ctor = ctors[0];
            if( ctor.getParameterCount() == 0)
                result = (Datum) ctors[0].newInstance();
            else if( ctor.getParameterCount() == 1)
                result = (Datum) ctors[0].newInstance( getter.get( _source ) );
            else
                throw new InstantiationException( "No constructor found for either 0 or 1 arguments: " + klass.getSimpleName() );
        }
        catch( InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e ) {
            String msg = "Couldn't construct Datum: " + klass.getSimpleName();
            LOG.error( msg, e );
            throw new HandlerIllegalStateException( msg, e );
        }

        return result;
    }


    public boolean isAuthorized( final Source _source ) {
        return authorizor.isAuthorized(_source );
    }


    public String getName() {
        return name;
    }
}
