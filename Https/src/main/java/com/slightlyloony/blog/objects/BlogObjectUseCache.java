package com.slightlyloony.blog.objects;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum BlogObjectUseCache {

    META(    0 ),
    IMAGE(   1 ),
    TEXT(    2 ),
    BINARY(  3 ),
    SCRIPT(  4 ),
    NONE(   -1 );


    private final int ordinal;


    BlogObjectUseCache( int _ordinal ) {
        ordinal = _ordinal;
    }


    public int getOrdinal() {
        return ordinal;
    }
}
