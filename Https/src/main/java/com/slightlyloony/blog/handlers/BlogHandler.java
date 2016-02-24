package com.slightlyloony.blog.handlers;

import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.events.EventType;
import com.slightlyloony.blog.events.Events;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.objects.BlogObjectType;
import com.slightlyloony.blog.responders.Responder;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.Timer;
import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.slightlyloony.blog.objects.ContentCompressionState.DO_NOT_COMPRESS;

/**
 * The one and only handler for the blog.  We are deliberately trading the complexity of the normal Jetty "handler chains" for the relative
 * simplicity (to us, anyway!) of straightforward procedural code.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogHandler extends AbstractHandler implements Handler {

    private static final Logger LOG = LogManager.getLogger();


    @Override
    public void handle( final String _s,
                        final Request _request, final HttpServletRequest _httpServletRequest, final HttpServletResponse _httpServletResponse )
            throws IOException, ServletException {

        Timer t = new Timer();

        LOG.info( LU.msg( "{0} {2}{1} from {3}", _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost() ) );

        BlogResponse response = new BlogResponse( _httpServletResponse );
        BlogRequest request = new BlogRequest( _request, _httpServletRequest, response );
        try {
            if( !request.initialize() ) {

                // TODO: handle invalid requests mo' bettah...
                response.setResponseCode( HttpServletResponse.SC_NOT_FOUND );
                request.handled();

                t.mark();

                LOG.info( LU.msg( "{0} {2}{1} from {3} completed (NOT FOUND) in {4}",
                        _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
                return;
            }
        }
        catch( StorageException e ) {

            // TODO: handle storage problem mo' bettah...
            response.setResponseCode( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            request.handled();

            t.mark();

            LOG.info( LU.msg( "{0} {2}{1} from {3} completed (but had error reading users) in {4}",
                    _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
            return;
        }

        // TODO: determine whether request is authorized...
        boolean authorized = request.getAccessRequirements().isAuthorized( request.getUser().getRights() );
        if( !authorized ) {

            // TODO: handle unauthorized without special responder mo' bettah...
            response.setResponseCode( HttpServletResponse.SC_FORBIDDEN );
            request.handled();

            t.mark();

            LOG.info( LU.msg( "{0} {2}{1} from {3} completed (but request is unauthorized) in {4}",
                    _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
            return;
    }

        // try to read the metadata for this request...
        BlogObjectMetadata metadata;
        try {
            metadata = (BlogObjectMetadata) BlogServer.STORAGE
                    .read( request.getId(), BlogObjectType.METADATA, request.getAccessRequirements(), DO_NOT_COMPRESS, true );
        }
        catch( StorageException e ) {

            // TODO: handle invalid objects mo' bettah...
            response.setResponseCode( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            request.handled();

            t.mark();

            LOG.info( LU.msg( "{0} {2}{1} from {3} completed (but resulting object is invalid) in {4}",
                    _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
            return;
        }

        // if this is an HTML page, update the session and stats...
        if( metadata.getContentType() == BlogObjectType.HTML ) {
            request.getSession().setLastPage( request.getId().getID() );
            Events.fire(EventType.PAGE_HIT, request.getSession() );
        }

        // get our responder, if we have one...
        Responder responder = metadata.getResponder( request.getRequestMethod() );
        if( responder == null ) {

            // TODO: handle no responder mo' bettah...
            response.setResponseCode( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            request.handled();

            t.mark();

            LOG.info( LU.msg( "{0} {2}{1} from {3} completed (but method type is not supported) in {4}",
                    _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
            return;
        }

        if( !request.accepts( metadata.getContentType().getMime() ) ) {

            // TODO: handle accept mismatch mo' bettah...
            response.setResponseCode( HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE );
            request.handled();

            t.mark();

            LOG.info( LU.msg( "{0} {2}{1} from {3} completed (but media type is not supported) in {4}",
                    _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
            return;
        }

        // take care of cache control...
        if( metadata.getExternalCacheSeconds() > 0 ) {
            response.setCacheControl( "public, no-transform, max-age=" + metadata.getExternalCacheSeconds() );
        }
        else {
            response.setCacheControl( "no-cache, no-transform, must-revalidate");
            response.setExpires( "Sat, 26 Jul 1997 05:00:00 GMT" );
        }

        try {
            responder.respond( request, response, metadata, metadata.isServerCacheable() );
        }
        catch( HandlerIllegalArgumentException | HandlerIllegalStateException | StorageException e ) {

            // TODO: handle error in request mo' bettah...
            response.setResponseCode( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            request.handled();

            t.mark();

            LOG.info( LU.msg( "{0} {2}{1} from {3} completed (but had a problem in responder) in {4}",
                    _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
            return;
        }

        t.mark();

        LOG.info( LU.msg( "{0} {2}{1} from {3} completed in {4}",
                _request.getMethod(), _s, _request.getHeader( "Host" ), _request.getRemoteHost(), t.toString() ) );
    }
}
