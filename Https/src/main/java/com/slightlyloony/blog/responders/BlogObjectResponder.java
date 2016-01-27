package com.slightlyloony.blog.responders;

import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.storage.StorageException;

/**
 * Handles the response when the content comes from a blog object (in other words, static content).
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObjectResponder implements Responder {

    @Override
    public void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata,
                         final boolean _isCacheable ) throws StorageException {

        BlogID content = _metadata.getContent();
        BlogObjectType contentType = _metadata.getContentType();
        ContentCompressionState compressionState = _metadata.getCompressionState();
        BlogContentObject obj;
        obj = (BlogContentObject) BlogServer.STORAGE.read( content, contentType, null, compressionState, _isCacheable );

        _response.setMimeType( _metadata.getContentType() );

        obj.getContent().write( _request, _response, _metadata.getCompressionState().mayCompress() );
        _request.handled();
    }
}
