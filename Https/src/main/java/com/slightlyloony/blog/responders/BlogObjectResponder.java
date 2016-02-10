package com.slightlyloony.blog.responders;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.templates.Template;
import com.slightlyloony.blog.templates.TemplateObject;
import com.slightlyloony.blog.templates.TemplateRenderingContext;
import com.slightlyloony.blog.templates.compiler.TemplateCompiler;
import com.slightlyloony.blog.templates.sources.RootSource;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.util.S;

import java.io.IOException;

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

        // if there's no template involved, we just read the file...
        if( _metadata.getSourceType() == null )
            obj = (BlogContentObject) BlogServer.STORAGE.read( content, contentType, null, compressionState, _isCacheable );

        // otherwise, we have some more work to do...
        else {

            // if we haven't already compiled the template object and cached it, we need to do so...
            obj = (TemplateObject) BlogServer.STORAGE.readCached( content, contentType );
            if( obj == null ) {

                // read the template source file (which the content points to), then compile it, and cache the result...
                BlogContentObject bco = (BlogContentObject) BlogServer.STORAGE.read( content, contentType, null, compressionState, false );
                try {
                    String source = S.fromUTF8( ByteStreams.toByteArray( bco.getStream() ) );
                    TemplateCompiler compiler = new TemplateCompiler();
                    Template template = compiler.compile( source );
                    obj = new TemplateObject( content, contentType, null, template );
                    BlogServer.STORAGE.cache( obj );
                }
                catch( IOException e ) {
                    throw new StorageException( "Problem reading or compiling template" );
                }
            }

            // now that we have a compiled template, we need a source and a user, and we need to set the context...
            RootSource source = _metadata.getSourceType().getSource( _request );
            User user = _request.getUser();
            TemplateRenderingContext.set( source, user );
        }

        _response.setMimeType( _metadata.getContentType() );

        obj.getContent().write( _request, _response, _metadata.getCompressionState().mayCompress() );
        if( obj instanceof TemplateObject )
            ((TemplateObject) obj).reset();
        _request.handled();
    }
}
