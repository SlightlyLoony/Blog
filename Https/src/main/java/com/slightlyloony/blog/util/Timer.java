package com.slightlyloony.blog.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a basic stopwatch-like facility, with the ability to log multiple events per timing session.  The time is captured in nanosecond
 * resolution, though not all Java Virtual Machines can capture time at that precision.  Instances of this class are not threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Timer {

    private static final Logger LOG = LogManager.getLogger();
    private static final DecimalFormat NS_FMT = new DecimalFormat( "##0 ns" );
    private static final DecimalFormat US_FMT = new DecimalFormat( "0.### µs" );
    private static final DecimalFormat MS_FMT = new DecimalFormat( "0.### ms" );
    private static final DecimalFormat S_FMT  = new DecimalFormat( "0.### s" );
    private static final DecimalFormat SS_FMT = new DecimalFormat( "00.### s" );
    private static final DecimalFormat M_FMT  = new DecimalFormat( "0:" );

    private List<Long> times;


    public Timer() {
        this( true );
    }


    public Timer( final boolean _autoStart ) {
        times = new ArrayList<>( 16 );
        if( _autoStart ) mark();
    }


    public void mark() {
        times.add( System.nanoTime() );
    }


    public void clear() {
        times.clear();
    }


    public String toString( final int _startMark, final int _endMark ) {

        if( (_startMark < 0) || (_startMark >= times.size()) || (_endMark < 0) || (_endMark >= times.size() || (_startMark >= _endMark)) ) {
            String msg = MessageFormat.format(
                    "Timer issue: tried to measure marks {1} to {2} with timer holding {0} captures", times.size(), _startMark, _endMark );
            LOG.warn( msg );
            return msg;
        }

        long nanoDiff = times.get( _endMark ) - times.get( _startMark );

        // determine scale (nanoseconds, microseconds, seconds, or minutes:seconds) and set up our variables...
        if( nanoDiff < 1_000 ) {
            return NS_FMT.format( nanoDiff );
        }
        else if( nanoDiff < 1_000_000 ) {
            return US_FMT.format( ((double) nanoDiff) / 1_000 );
        }
        else if( nanoDiff < 1_000_000_000 ) {
            return MS_FMT.format( ((double) nanoDiff) / 1_000_000 );
        }
        else if( nanoDiff < 60_000_000_000L ) {
            return S_FMT.format( ((double) nanoDiff) / 1_000_000_000 );
        }
        else {
            long minutes = nanoDiff / 60_000_000_000L;
            long nanoseconds = nanoDiff % 60_000_000_000L;
            return M_FMT.format( minutes ) + SS_FMT.format( ((double) nanoDiff) / 1_000_000_000 );
        }
    }


    public String toString( final int _endMark ) {
        return toString( 0, _endMark );
    }


    public String toString() {
        return toString( 0, times.size() - 1 );
    }
}
