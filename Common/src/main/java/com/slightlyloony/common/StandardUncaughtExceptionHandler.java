package com.slightlyloony.common;

import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StandardUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {


    private static Logger LOG = LogManager.getLogger();


    /**
     * Method invoked when the given thread terminates due to the given uncaught exception. <p>Any exception thrown by this method will be ignored by
     * the Java Virtual Machine.
     *
     * @param t the thread
     * @param e the exception
     */
    @Override
    public void uncaughtException( final Thread t, final Throwable e ) {

        LOG.error( LU.msg( "Thread \"{0}\" terminated unexpectedly because of this exception:", t.getName() ), e );
    }
}
