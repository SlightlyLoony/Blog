package com.slightlyloony.blog.objects;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum ContentCompressionState {

    UNCOMPRESSED    ( false, true  ),
    DO_NOT_COMPRESS ( false, false ),
    COMPRESSED      ( true,  false );


    private final boolean compressed;
    private final boolean mayCompress;


    ContentCompressionState( final boolean _compressed, final boolean _mayCompress ) {
        compressed = _compressed;
        mayCompress = _mayCompress;
    }


    public boolean isCompressed() {
        return compressed;
    }


    public boolean mayCompress() {
        return mayCompress;
    }
}
