package com.slightlyloony.logging;

import org.junit.Test;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class LoggerTest {

    @Test
    public void simpleTest() {
        Logger logger = Log.get( LoggerTest.class );
        logger.warningWithStack( "Test {1}", new IllegalStateException( "this" ), "that" );
        hashCode();
    }
}