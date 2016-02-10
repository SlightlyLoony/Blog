package com.slightlyloony.blog.objects;

import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.storage.StorageInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogContentObject extends BlogObject {

    private final static Logger LOG = LogManager.getLogger();

    protected BlogObjectContent content;  // if this field is null, then the instance is invalid...


    public BlogContentObject( final BlogID _id, final BlogObjectType _type,
                       final BlogObjectAccessRequirements _accessRequirements, final BlogObjectContent _content ) {
        super( _id, _type, _accessRequirements );

        content = _content;
    }


    /**
     * Resolves this instance into bytes, compressed if possible.
     */
    public synchronized void makeReadyForCache( final boolean _mayCompress ) {

        // make sure we have bytes, and attempt to compress them...
        content = content.asCompressedBytes( _mayCompress );
    }


    @Override
    public synchronized int size() {
        return baseSize() + 8 + content.size();
    }


    public Integer contentLength() {
        return content.contentLength();
    }


    public synchronized BlogObjectContent getContent() {
        return content;
    }


    public synchronized StorageInputStream getStream() {
        return content.asStream().getStream();
    }


    public synchronized byte[] getBytes() {
        return content.asBytes().getBytes();
    }
}
