package com.slightlyloony.blog.responders;

import com.google.gson.Gson;
import com.slightlyloony.blog.Blog;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.handlers.Constants;
import com.slightlyloony.blog.handlers.cookies.ResponseCookie;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Handles the response to a user logout request, which is a POST of a JSON object with one property: "signOut", which must be true.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class UserLogoutResponder implements Responder {

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
            LogoutRequest req = new Gson().fromJson( json, LogoutRequest.class );
            LOG.info( "Got user logout request" );

            // if we got a proper request...
            if( req.signOut ) {

                // remove user from the session...
                _request.logoutUser();

                // set a junk cookie to expire immediately...
                Blog blog = _request.getBlog();
                ResponseCookie cookie = new ResponseCookie( Constants.USER_COOKIE_NAME, "", blog.getName(), "/" );
                cookie.setLifetimeSeconds( -30 * 24 * 3600 );  // 30 days ago in seconds...
                _response.addCookie( cookie );

                // send a success response...
                _response.sendJSONResponse( "{\"success\":true}" );
            }

            _request.handled();
        }
        catch( IOException e ) {
            throw new StorageException( "Problem responding to user logout: " + e.getMessage(), e );
        }
    }


    private static class LogoutRequest {
        private boolean signOut;
    }
}
