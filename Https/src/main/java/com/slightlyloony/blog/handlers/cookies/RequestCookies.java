package com.slightlyloony.blog.handlers.cookies;

import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RequestCookie collection class, with methods to parse request cookie headers.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RequestCookies {

    private final Map<String,RequestCookie> cookieMap;


    public RequestCookies( final BlogRequest _request ) {

        if( _request == null )
            throw new HandlerIllegalArgumentException( "Missing request" );

        cookieMap = new HashMap<>();
        List<String> cookies = _request.getHeaders( "Cookie" );
        for( String cookie : cookies ) {

            // may have multiple ";"-separated cookie name/value pairs in one cookie header...
            String[] subCookies = cookie.split( ";" );

            for( String subCookie : subCookies ) {

                // try to parse it into a request cookie...
                RequestCookie requestCookie = RequestCookie.parse( subCookie );

                // if we got the cookie, then add it to our collection...
                if( requestCookie != null )
                    cookieMap.put( requestCookie.getName().toLowerCase(), requestCookie );
            }
        }
    }


    public RequestCookie get( final String _name ) {

        if( _name == null )
            throw new HandlerIllegalArgumentException( "Missing name" );

        return cookieMap.get( _name.toLowerCase() );
    }
}
