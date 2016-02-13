package com.slightlyloony.blog.handlers;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.cookies.ResponseCookie;
import com.slightlyloony.blog.handlers.cookies.ResponseCookies;
import com.slightlyloony.blog.objects.BlogObjectType;
import com.slightlyloony.blog.util.S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogResponse {

    private static final Logger LOG = LogManager.getLogger();

    private final HttpServletResponse response;
    private final ResponseCookies cookies;


    public BlogResponse( final HttpServletResponse _response ) {

        response = _response;
        response.setHeader( "Server", "SlightlyBloggy" );
        cookies = new ResponseCookies();
    }


    public void addCookie( final ResponseCookie _cookie ) {
        cookies.add( _cookie );
        response.addHeader( "Set-Cookie", _cookie.toString() );
    }


    public void setContentLength( final int _length ) {
        response.setContentLength( _length );
    }


    public void setMimeType( final BlogObjectType _mimeType ) {

        if( _mimeType == null)
            throw new HandlerIllegalArgumentException( "Missing MIME type" );

        response.setContentType( _mimeType.getMime() );
    }


    public void setResponseCode( final int _statusCode ) {
        response.setStatus( _statusCode );
    }


    public void setContentEncoding( final String _contentEncoding ) {
        response.setHeader( "Content-Encoding", _contentEncoding );
    }


    public void setCacheControl( final String _cacheControl ) {
        response.setHeader( "Cache-Control", _cacheControl );
    }


    public void setExpires( final String _expires ) {
        response.setHeader( "Expires", _expires );
    }


    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }


    public void sendJSONResponse( final String _json ) throws IOException {
        InputStream is = new ByteArrayInputStream( S.toUTF8( _json ) );
        ByteStreams.copy( is, getOutputStream() );
    }
}
