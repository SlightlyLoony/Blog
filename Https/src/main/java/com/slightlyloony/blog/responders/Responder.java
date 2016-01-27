package com.slightlyloony.blog.responders;

import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.storage.StorageException;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Responder {

    void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata,
                  final boolean _isCacheable ) throws StorageException;
}
