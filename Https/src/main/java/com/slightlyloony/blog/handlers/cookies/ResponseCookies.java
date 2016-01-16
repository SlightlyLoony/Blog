package com.slightlyloony.blog.handlers.cookies;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ResponseCookies {

    private final List<ResponseCookie> cookies;


    public ResponseCookies() {
        cookies = new ArrayList<>();
    }


    public void add( final ResponseCookie _cookie ) {
        cookies.add( _cookie );
    }
}
