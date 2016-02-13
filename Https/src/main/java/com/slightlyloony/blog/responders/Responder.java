package com.slightlyloony.blog.responders;

import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.storage.StorageException;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Responder {

    /**
     * Handles the response to a request.
     *
     * @param _request the blog request object for this request
     * @param _response the blog response object for this request
     * @param _metadata the metadata for this request
     * @param _isCacheable true if this request is cacheable
     * @throws StorageException on any problem
     */
    void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata,
                  final boolean _isCacheable ) throws StorageException;
}
