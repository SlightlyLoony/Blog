package com.slightlyloony.common.ipmsgs;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IPSequenceData extends IPData {

    private long sequence;


    public IPSequenceData( final long _sequence ) {
        sequence = _sequence;
    }


    public long getSequence() {
        return sequence;
    }
}
