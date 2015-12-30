package com.slightlyloony.logging;

import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * Static container class with a conveniently short name that simplifies use of java.util.logging.
 *
 * Upon initialization, a default handler and formatter is set up to log to the console (system out).  This can be easily changed by methods that
 * change the global Logger.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public final class Log {


    public static Logger get( final Class _class ) {
        return get( _class, null, null );
    }


    public static Logger get( final Class _class, final Filter _filter ) {
        return get( _class, null, _filter );
    }


    public static Logger get( final Class _class, final Level _level ) {
        return get( _class, _level, null );
    }


    public static Logger get( final Class _class, final Level _level, final Filter _filter ) {

        Logger logger = new Logger( java.util.logging.Logger.getLogger( _class.getName() ) );

        if( _level != null )
            logger.setLevel( _level );

        if( _filter != null )
            logger.setFilter( _filter );

        return logger;
    }


    /*
     * Prevent instantiation of this static container class.
     */
    private Log() {
        // naught to do...
    }
}
