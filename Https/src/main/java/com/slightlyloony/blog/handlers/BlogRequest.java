package com.slightlyloony.blog.handlers;

import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.config.BlogConfig;
import com.slightlyloony.blog.handlers.cookies.RequestCookie;
import com.slightlyloony.blog.handlers.cookies.RequestCookies;
import com.slightlyloony.blog.handlers.cookies.ResponseCookie;
import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.security.BlogSession;
import com.slightlyloony.blog.security.BlogSessionManager;
import com.slightlyloony.blog.security.BlogUserRights;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogRequest {

    private final Request request;
    private final HttpServletRequest httpServletRequest;
    private final BlogResponse response;
    private final Pattern HOST_PATTERN = Pattern.compile( ".*?(\\w*\\.\\w*)(:\\d{1,5})?" );

    private RequestMethod requestMethod;
    private String errorMessage;
    private String blog;
    private BlogConfig blogConfig;
    private BlogID id;
    private BlogObjectAccessRequirements accessRequirements;
    private BlogUserRights rights;
    private BlogSession session;
    private RequestCookies cookies;

    public BlogRequest( final Request _request, final HttpServletRequest _httpServletRequest, final BlogResponse _response ) {

        request = _request;
        httpServletRequest = _httpServletRequest;
        response = _response;
    }


    /**
     * Initializes this blog request, and returns true if the request is valid.  If this method returns false, then the {@link #getErrorMessage()
     * getErrorMessage} method contains an explanatory message.
     *
     * @return true if the request is valid
     */
    public boolean initialize() {

        if( !initializeRequestMethod() )  return false;
        if( !initializeBlogName() )       return false;
        if( !initializePathParts() )      return false;
        if( !initializeBlogUserRights() ) return false;

        cookies = new RequestCookies( this );
        initializeSession();

        return true;
    }


    private void initializeSession() {

        // if we've already got a session, claim it...
        RequestCookie sessionCookie = cookies.get( Constants.SESSION_COOKIE_NAME );
        if( sessionCookie != null )
            session = BlogSessionManager.INSTANCE.claimSession( sessionCookie.getValue() );

        // if we don't have a session, and it's a public URI, then we should have one...
        if( (session == null) && (accessRequirements == BlogObjectAccessRequirements.PUBLIC) ) {

            // create a new session...
            session = BlogSessionManager.INSTANCE.create();

            // if we succeeded, then set a name and set a session cookie...
            if( session != null ) {
                session.setName( httpServletRequest.getRemoteAddr() );  // default the name to the client's IP address...
                response.addCookie( new ResponseCookie( Constants.SESSION_COOKIE_NAME, session.getToken(), blog, "/" ) );
            }
        }
    }


    private boolean initializeBlogUserRights() {
        return true;
    }


    private boolean initializePathParts() {
        String path = blogConfig.map( request.getPathInfo() );
        if( (path == null) || (path.length() != 12) ) {
            errorMessage = "Malformed URI path: " + path + " (path in request was: " + request.getPathInfo() + ")";
            return false;
        }
        id = BlogID.create( path.substring( 1, 11 ) );
        if( id == null) {
            errorMessage = "Invalid blog ID: " + path.substring( 1, 11 );
            return false;
        }
        accessRequirements = BlogObjectAccessRequirements.get( path.charAt( 11 ) );
        if( accessRequirements == null ) {
            errorMessage = "Invalid access requirements: " + path.charAt( 11 );
            return false;
        }
        return true;
    }


    private boolean initializeRequestMethod() {
        requestMethod = RequestMethod.valueOf( request.getMethod() );
        if( requestMethod == null ) {
            errorMessage = "Invalid request type (method): " + request.getMethod();
            return false;
        }
        return true;
    }


    private boolean initializeBlogName() {
        String host = request.getHeader( "Host" );
        Matcher mat = HOST_PATTERN.matcher( host );
        if( !mat.matches() ) {
            errorMessage = "Invalid host in request: " + host;
            return false;
        }
        blog = mat.group( 1 );
        blogConfig = ServerInit.getBlogConfig( blog );
        if( blogConfig == null ) {
            errorMessage = "Unknown or unconfigured blog: " + blog;
            return false;
        }
        return true;
    }


    public List<String> getHeaders( final String _headerName ) {

        Enumeration<String> enumeration = httpServletRequest.getHeaders( _headerName );
        List<String> result = new ArrayList<>();
        while( enumeration.hasMoreElements() )
            result.add( enumeration.nextElement() );
        return result;
    }


    public void handled() {
        request.setHandled( true );
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    public RequestMethod getRequestMethod() {
        return requestMethod;
    }


    public String getBlog() {
        return blog;
    }


    public BlogConfig getBlogConfig() {
        return blogConfig;
    }


    public BlogID getId() {
        return id;
    }


    public BlogObjectAccessRequirements getAccessRequirements() {
        return accessRequirements;
    }
}
