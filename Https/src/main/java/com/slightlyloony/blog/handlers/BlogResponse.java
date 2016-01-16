package com.slightlyloony.blog.handlers;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.cookies.ResponseCookie;
import com.slightlyloony.blog.handlers.cookies.ResponseCookies;
import com.slightlyloony.blog.objects.BlogObjectType;
import com.slightlyloony.blog.storage.StorageInputStream;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogResponse {

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


    public void setMimeType( final BlogObjectType _mimeType ) {

        if( _mimeType == null)
            throw new IllegalArgumentException( "Missing MIME type" );

        response.setContentType( _mimeType.getMime() );
    }


    public void write( final StorageInputStream _inputStream ) {

        response.setContentLength( _inputStream.length() );

        try( InputStream is = _inputStream;
             OutputStream os = response.getOutputStream(); ) {

            ByteStreams.copy( is, os );
            os.flush();
        }
        catch( IOException e ) {
            // TODO: handle this mo' bettah...
            e.printStackTrace();
        }
    }
}
