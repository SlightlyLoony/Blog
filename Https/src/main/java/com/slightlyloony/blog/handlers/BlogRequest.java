package com.slightlyloony.blog.handlers;

import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.config.BlogConfig;
import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.security.BlogUserRights;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogRequest {

    private final Request request;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final Pattern HOST_PATTERN = Pattern.compile( ".*?(\\w*\\.\\w*)(:\\d{1,5})?" );

    private RequestMethod requestMethod;
    private String errorMessage;
    private String blog;
    private BlogConfig blogConfig;
    private BlogID id;
    private BlogObjectAccessRequirements accessRequirements;
    private BlogUserRights rights;

    public BlogRequest( final Request _request, final HttpServletRequest _httpServletRequest, final HttpServletResponse _httpServletResponse ) {

        request = _request;
        httpServletRequest = _httpServletRequest;
        httpServletResponse = _httpServletResponse;
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
        if( !initializeSession() )        return false;
        if( !initializeBlogUserRights() ) return false;

        return true;
    }


    private boolean initializeSession() {
        return true;
//        // if we're already in a session, just leave with success...
//        if( request.getSession() != null )
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
