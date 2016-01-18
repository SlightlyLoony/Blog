package com.slightlyloony.blog.objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents an object stored on the blog, which could be metadata, static (including template generated) content, or content generated by code.
 * The class includes provision for either storing the encoded byte form of the object, or as an input stream with known length.  When an instance
 * of this class resides in a cache, it is <i>always</i> in encoded byte form, compressed if possible, to minimize the memory consumed.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObject {

    private final static Logger LOG = LogManager.getLogger();
    private final static int ASSUMED_OVERHEAD_BYTES = 50;

    private final BlogID blogID;
    private final BlogObjectType type;

    private BlogObjectContent content;  // if this field is null, then the instance is invalid...


    public BlogObject( final BlogID _id, final BlogObjectType _type, final BlogObjectContent _content ) {

        if( (_type == null) || (_id == null) )
            throw new IllegalArgumentException( "Missing required argument _type or _id" );

        type = _type;
        content = _content;
        blogID = _id;
    }


    public BlogObject( final BlogID _id, final BlogObjectType _type ) {
        this( _id, _type, null );
    }


    /**
     * Resolves this instance into bytes, compressed if possible.
     */
    public synchronized void makeReadyForCache( final boolean _mayCompress ) {

        // if we've got an invalid instance, we have problems...
        if( !isValid() ) {
            String msg = "Attempted to compress an invalid instance, ID: " + blogID;
            LOG.error( msg );
            throw new IllegalStateException( msg );
        }

        // make sure we have bytes, and attempt to compress them...
        content = content.asCompressedBytes( _mayCompress );
    }


    public synchronized boolean isValid() {
        return content != null;
    }


    public BlogID getBlogID() {
        return blogID;
    }


    public BlogObjectType getType() {
        return type;
    }


    public synchronized int memorySize() {
        return ASSUMED_OVERHEAD_BYTES + ((content == null) ? 0 : content.memorySize() );
    }


    public synchronized int contentLength() {
        return (content == null) ? 0 : content.contentLength;
    }


    public synchronized BlogObjectContent getContent() {
        return content;
    }
}
