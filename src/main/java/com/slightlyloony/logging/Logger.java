package com.slightlyloony.logging;

import com.google.common.base.Throwables;

import java.text.MessageFormat;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Logger {

    private final java.util.logging.Logger logger;


    Logger( final java.util.logging.Logger _logger ) {
        logger = _logger;
    }


    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   _level   One of the message level identifiers, e.g., SEVERE
     * @param   _msg     The string message
     * @param   _throwable A throwable to include in the log record
     * @param   _params  array of parameters to the message
     */
    public void logWithStack( final Level _level, final String _msg, final Throwable _throwable, final Object... _params ) {
        logger.log( _level, MessageFormat.format( _msg, _params ) + "\n" + Throwables.getStackTraceAsString( _throwable ) );
    }


    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   _level   One of the message level identifiers, e.g., SEVERE
     * @param   _msg     The string message
     * @param   _throwable A throwable to include in the log record
     * @param   _params  array of parameters to the message
     */
    public void log( final Level _level, final String _msg, final Throwable _throwable, final Object... _params ) {
        logger.log( _level, _msg, _params );
    }


    /**
     * Log a message, with a throwable and an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     * <p>
     * @param   _level   One of the message level identifiers, e.g., SEVERE
     * @param   _msg     The string message (or a key in the message catalog)
     * @param   _params  array of parameters to the message
     */
    public void log( final Level _level, final String _msg, final Object... _params ) {
        logger.log( _level, _msg, _params );
    }


    public void severe( final String _msg, final Object... _params ) {
        log( Level.SEVERE, _msg, _params );
    }


    public void warning( final String _msg, final Object... _params ) {
        log( Level.WARNING, _msg, _params );
    }


    public void info( final String _msg, final Object... _params ) {
        log( Level.INFO, _msg, _params );
    }


    public void config( final String _msg, final Object... _params ) {
        log( Level.CONFIG, _msg, _params );
    }


    public void fine( final String _msg, final Object... _params ) {
        log( Level.FINE, _msg, _params );
    }


    public void finer( final String _msg, final Object... _params ) {
        log( Level.FINER, _msg, _params );
    }


    public void finest( final String _msg, final Object... _params ) {
        log( Level.FINEST, _msg, _params );
    }


    public void severe( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.SEVERE, _msg, _throwable, _params );
    }


    public void warning( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.WARNING, _msg, _throwable, _params );
    }


    public void info( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.INFO, _msg, _throwable, _params );
    }


    public void config( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.CONFIG, _msg, _throwable, _params );
    }


    public void fine( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.FINE, _msg, _throwable, _params );
    }


    public void finer( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.FINER, _msg, _throwable, _params );
    }


    public void finest( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.FINEST, _msg, _throwable, _params );
    }


    public void severeWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        log( Level.SEVERE, _msg, _throwable, _params );
    }


    public void warningWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        logWithStack( Level.WARNING, _msg, _throwable, _params );
    }


    public void infoWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        logWithStack( Level.INFO, _msg, _throwable, _params );
    }


    public void configWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        logWithStack( Level.CONFIG, _msg, _throwable, _params );
    }


    public void fineWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        logWithStack( Level.FINE, _msg, _throwable, _params );
    }


    public void finerWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        logWithStack( Level.FINER, _msg, _throwable, _params );
    }


    public void finestWithStack( final String _msg, final Throwable _throwable, final Object... _params ) {
        logWithStack( Level.FINEST, _msg, _throwable, _params );
    }


    /**
     * Set a filter to control output on this Logger.
     * <P>
     * After passing the initial "level" check, the Logger will
     * call this Filter to check if a log record should really
     * be published.
     *
     * @param   _filter  a filter object (may be null)
     * @throws SecurityException if a security manager exists,
     *          this logger is not anonymous, and the caller
     *          does not have LoggingPermission("control").
     */
    public void setFilter( final Filter _filter ) throws SecurityException {
        logger.setFilter( _filter );
    }


    /**
     * Get the current filter for this Logger.
     *
     * @return a filter object (may be null)
     */
    public Filter getFilter() {
        return logger.getFilter();
    }


    /**
     * Set the log level specifying which message levels will be
     * logged by this logger.  Message levels lower than this
     * value will be discarded.  The level value Level.OFF
     * can be used to turn off logging.
     * <p>
     * If the new level is null, it means that this node should
     * inherit its level from its nearest ancestor with a specific
     * (non-null) level value.
     *
     * @param _level   the new value for the log level (may be null)
     * @throws SecurityException if a security manager exists,
     *          this logger is not anonymous, and the caller
     *          does not have LoggingPermission("control").
     */
    public void setLevel( final Level _level ) throws SecurityException {
        logger.setLevel( _level );
    }


    /**
     * Get the log Level that has been specified for this Logger.
     * The result may be null, which means that this logger's
     * effective level will be inherited from its parent.
     *
     * @return this Logger's level
     */
    public Level getLevel() {
        return logger.getLevel();
    }


    /**
     * Get the name for this logger.
     * @return logger name.  Will be null for anonymous Loggers.
     */
    public String getName() {
        return logger.getName();
    }


    /**
     * Add a log Handler to receive logging messages.
     * <p>
     * By default, Loggers also send their output to their parent logger.
     * Typically the root Logger is configured with a set of Handlers
     * that essentially act as default handlers for all loggers.
     *
     * @param   _handler a logging Handler
     * @throws SecurityException if a security manager exists,
     *          this logger is not anonymous, and the caller
     *          does not have LoggingPermission("control").
     */
    public void addHandler( final Handler _handler ) throws SecurityException {
        logger.addHandler( _handler );
    }


    /**
     * Remove a log Handler.
     * <P>
     * Returns silently if the given Handler is not found or is null
     *
     * @param   _handler a logging Handler
     * @throws SecurityException if a security manager exists,
     *          this logger is not anonymous, and the caller
     *          does not have LoggingPermission("control").
     */
    public void removeHandler( final Handler _handler ) throws SecurityException {
        logger.removeHandler( _handler );
    }


    /**
     * Specify whether or not this logger should send its output
     * to its parent Logger.  This means that any LogRecords will
     * also be written to the parent's Handlers, and potentially
     * to its parent, recursively up the namespace.
     *
     * @param _useParentHandlers   true if output is to be sent to the
     *          logger's parent.
     * @throws SecurityException if a security manager exists,
     *          this logger is not anonymous, and the caller
     *          does not have LoggingPermission("control").
     */
    public void setUseParentHandlers( final boolean _useParentHandlers ) {
        logger.setUseParentHandlers( _useParentHandlers );
    }


    /**
     * Get the Handlers associated with this logger.
     * <p>
     * @return an array of all registered Handlers
     */
    public Handler[] getHandlers() {
        return logger.getHandlers();
    }


    /**
     * Discover whether or not this logger is sending its output
     * to its parent logger.
     *
     * @return true if output is to be sent to the logger's parent
     */
    public boolean getUseParentHandlers() {
        return logger.getUseParentHandlers();
    }


    /**
     * Return the parent for this Logger.
     * <p>
     * This method returns the nearest extant parent in the namespace.
     * Thus if a Logger is called "a.b.c.d", and a Logger called "a.b"
     * has been created but no logger "a.b.c" exists, then a call of
     * getParent on the Logger "a.b.c.d" will return the Logger "a.b".
     * <p>
     * The result will be null if it is called on the root Logger
     * in the namespace.
     *
     * @return nearest existing parent Logger
     */
    public java.util.logging.Logger getParent() {
        return logger.getParent();
    }


    /**
     * Set the parent for this Logger.  This method is used by
     * the LogManager to update a Logger when the namespace changes.
     * <p>
     * It should not be called from application code.
     * <p>
     * @param  _parent   the new parent logger
     * @throws SecurityException  if a security manager exists and if
     *          the caller does not have LoggingPermission("control").
     */
    public void setParent( final java.util.logging.Logger _parent ) {
        logger.setParent( _parent );
    }
}
