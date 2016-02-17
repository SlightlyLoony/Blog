package com.slightlyloony.blog.responders;

import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Handles the response to a statistics request
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StatsResponder implements Responder {

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

            // generate a stats report and send it to the client...
            _response.sendJSONResponse( Stats.report() );

            LOG.info( "Sent stats report" );

            _request.handled();
        }
        catch( IOException e ) {
            throw new StorageException( "Problem responding to stats request: " + e.getMessage(), e );
        }
    }


    private static class LoginRequest {
        private String user;
        private String password;
    }
}
