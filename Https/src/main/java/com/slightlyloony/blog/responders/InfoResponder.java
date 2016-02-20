package com.slightlyloony.blog.responders;

import com.google.gson.Gson;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.events.EventType;
import com.slightlyloony.blog.events.Events;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.objects.Info;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.slightlyloony.blog.objects.BlogObjectType.INFO;
import static com.slightlyloony.blog.objects.ContentCompressionState.UNCOMPRESSED;

/**
 * Handles the response to an info request.  The request is JSON object with a single property ("key"), which should be a valid key to an info record
 * (see {@link Info}).  The response is always a JSON object with two properties ("key" and "info"), which will be the key sent and the associated
 * value.  If the key is invalid, then the response value will be the empty string.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class InfoResponder implements Responder {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Handles the response to a user login request.
     *
     * @param _request the blog request object for this request
     * @param _response the blog response object for this request
     * @param _metadata the metadata for this request
     * @param _isCacheable true if this request is cacheable
     * @throws StorageException on any problem
     */
    @Override
    public void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata, final boolean _isCacheable )
            throws StorageException {

        try {
            // decode the request...
            String json = S.fromUTF8( _request.getPostData() );
            InfoRequest req = new Gson().fromJson( json, InfoRequest.class );
            LOG.info( "Got info request for key: " + req.key );

            // get our information object...
            Info info = (Info) BlogServer.STORAGE.read( _metadata.getContent(), INFO, null, UNCOMPRESSED, true );

            // retrieve the associated information...
            req.info = info.get( req.key );

            // and return it...
            _response.sendJSONResponse( new Gson().toJson( req ) );

            // fire success event...
            Events.fire( EventType.INFO_REQUEST, req.key );

            _request.handled();
        }
        catch( IOException e ) {
            throw new StorageException( "Problem responding to user login: " + e.getMessage(), e );
        }
    }


    private static class InfoRequest {
        private String key;
        private String info;
    }
}
