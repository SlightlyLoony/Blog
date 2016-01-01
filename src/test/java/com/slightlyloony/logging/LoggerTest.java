package com.slightlyloony.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class LoggerTest {

    @Test
    public void simpleTest() {
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        Logger logger = LogManager.getLogger();
        logger.info( "Test" );

        Throwable e = new IllegalStateException( "Dang it!" );
        logger.error( LU.msg( "Problem: {0}", e.getMessage() ), e );
        logger.warn( "Test" );

        try {
            try {
                throw new IllegalStateException( "...and the inner inner beast" );
            }
            catch (IllegalStateException ise ) {
                throw new IllegalArgumentException( "Here's my inner beast!", ise );
            }
        }

        catch( IllegalArgumentException iae ) {
            logger.error( LU.msg( "Here's the beast!" ), iae );
        }
    }
}