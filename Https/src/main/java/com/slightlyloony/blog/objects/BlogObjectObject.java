package com.slightlyloony.blog.objects;

import com.slightlyloony.blog.security.BlogObjectAccessRequirements;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class BlogObjectObject extends BlogObject {


    protected BlogObjectObject( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements ) {
        super( _id, _type, _accessRequirements );
    }


    protected BlogObjectObject() {
        // for use by deserializers only...
    }


    /**
     * Resolves this instance into cacheable form, possibly compressed (if content).
     *
     * @param _mayCompress true if this instance may be compressed
     */
    @Override
    public void makeReadyForCache( final boolean _mayCompress ) {
        // do nothing...
    }
}
