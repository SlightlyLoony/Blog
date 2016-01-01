package com.slightlyloony.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Monitor {


    private static Logger LOG;


    public static void main( final String[] _args ) throws InterruptedException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // now we can set up our logger...
        LOG = LogManager.getLogger();

        // initialization of the monitor application...
        Init.init();

        while(true)
            Thread.sleep( 1000 );
    }
}
