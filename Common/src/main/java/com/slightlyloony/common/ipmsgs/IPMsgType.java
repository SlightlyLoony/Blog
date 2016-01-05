package com.slightlyloony.common.ipmsgs;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum IPMsgType {

    ProcessAlive   ( 0,   IPSequenceData.class ),
    WebAlive       ( 1,   null                 ),
    StartWeb       ( 2,   null                 ),
    StopWeb        ( 3,   null                 ),
    Shutdown       ( 4,   null                 ),
    ShuttingDown   ( 5,   null                 );


    private static Map<Integer, IPMsgType> LOOKUP;
    private final int ordinal;
    private final String fqDataClassName;


    IPMsgType( final int _ordinal, final Class<? extends IPData> _class ) {
        ordinal = _ordinal;
        fqDataClassName = (_class == null) ? null : _class.getName();
        addMapping( ordinal, this );
    }


    private static void addMapping( final int _ordinal, final IPMsgType _type ) {
        if( LOOKUP == null )
            LOOKUP = Maps.newHashMap();
        LOOKUP.put( _ordinal, _type );
    }


    public int getOrdinal() {
        return ordinal;
    }


    public static IPMsgType fromOrdinal( final int _ordinal ) {
        return LOOKUP.get( _ordinal );
    }


    public String getFqDataClassName() {
        return fqDataClassName;
    }
}
