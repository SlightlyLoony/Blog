package com.slightlyloony.blog.responders;

import com.google.gson.Gson;
import com.slightlyloony.blog.events.EventType;
import com.slightlyloony.blog.events.Events;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.util.S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static com.slightlyloony.blog.security.BlogAccessRight.*;

/**
 * Handles the response to a user login request, which is a POST of a JSON object with two properties: "user", which must have a string username,
 * and "password", which must have a string password.  The response is also a JSON object, with either one or two properties.  It will always
 * have a property "success", which is a boolean that is true if the login succeeded.  The other property is present only on failure; it is
 * named "reason", and is a string field describing the reason that the login attempt failed.  If the login succeeded, the user's object is added
 * to the session, and the session gains logged-in user rights.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class UserLoginResponder implements Responder {

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
            LoginRequest req = new Gson().fromJson( json, LoginRequest.class );
            LOG.info( "Got user login request for user: " + req.user );

            // retrieve the user and see if the password matches...
            User user = _request.getBlog().getUsers().getUserFromUsername( req.user );
            if( (user != null) && user.passwordOK( req.password ) ) {

                // send a success response...
                _response.sendJSONResponse( "{\"success\":true}" );

                // put this user in our session, with authenticated rights...
                user.addRight( AUTHENTICATED );
                user.addRight( PUBLIC );
                user.addRight( SESSION );
                _request.getSession().putUser( user );

                // fire success event...
                Events.fire( EventType.USER_LOGIN, _request.getSession() );
            }

            // otherwise, the login attempt failed...
            else {

                // tell the client about our dismal failure, being careful not to disclose why...
                _response.sendJSONResponse( "{\"success\":false,\"reason\":\"User name or password is incorrect.\"}" );

                // fire failure event...
                Events.fire( EventType.USER_LOGIN_FAILURE, req.user );
            }

            _request.handled();
        }
        catch( IOException e ) {
            throw new StorageException( "Problem responding to user login: " + e.getMessage(), e );
        }
    }


    private static class LoginRequest {
        private String user;
        private String password;
    }
}
