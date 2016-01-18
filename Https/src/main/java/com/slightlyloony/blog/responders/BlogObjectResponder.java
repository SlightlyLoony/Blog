package com.slightlyloony.blog.responders;

import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogObject;
import com.slightlyloony.blog.objects.BlogObjectMetadata;

/**
 * Handles the response when the content comes from a blog object (in other words, static content).
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObjectResponder implements Responder {

    @Override
    public void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata, final boolean isAuthorized ) {

        if( isAuthorized ) {

            BlogObject obj = BlogServer.STORAGE.read( _metadata.getContent(), _metadata.getType(), null, _metadata.mayCompress() );
            _response.setMimeType( _metadata.getType() );

            obj.getContent().write( _request, _response, _metadata.mayCompress() );
            _request.handled();
        }

        else {
            // TODO: what do we do if this is unauthorized?
        }
    }
}
