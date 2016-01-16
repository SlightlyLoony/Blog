package com.slightlyloony.blog.handlers.cookies;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ResponseCookie {

    private String name;
    private String value;
    private Instant expires;
    private Long maxAge;
    private String domain;
    private String path;
    private boolean httpOnly;
    private boolean secure;


    /**
     * Creates a new instance of this class with the given attributes.  The created cookie has no expiration time (a session cookie), has both the
     * HttpOnly and Secure attributes set.
     *
     * @param _name the name of this cookie
     * @param _value the value of this cookie (which must <i>not</i> have any invalid characters in it)
     * @param _domain the domain this cookie applies to
     * @param _path the path this cookie applies to (within the given domain)
     */
    public ResponseCookie( final String _name, final String _value, final String _domain, final String _path ) {

        name = _name;
        value = _value;
        expires = null;
        maxAge = null;
        domain = _domain;
        path = _path;
        httpOnly = true;
        secure = true;
    }


    /**
     * Sets the lifetime of this cookie.  For maximum compatibility, this results in <i>both</i> the expiration date/time and the maximum age
     * attributes being set.
     *
     * @param _lifetime the lifetime of this cookie, in seconds
     */
    public void setLifetimeSeconds( final long _lifetime ) {

        maxAge = _lifetime;
        expires = Instant.now().plus( maxAge, ChronoUnit.SECONDS );
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder( 128 );
        sb.append( name );
        sb.append( '=' );
        sb.append( value );
        if( expires != null ) {
            sb.append( "; Expires=" );
            DateTimeFormatter dtf = DateTimeFormatter.RFC_1123_DATE_TIME;
            sb.append( dtf.format( expires ) );
        }
        if( maxAge != null ) {
            sb.append( "; Max-Age+" );
            sb.append( maxAge );
        }
        sb.append( "; Domain=" );
        sb.append( domain );
        sb.append( "; Path=" );
        sb.append( path );
        if( secure )
            sb.append( "; Secure" );
        if( httpOnly )
            sb.append( "; HttpOnly" );
        return sb.toString();
    }


    public void setHttpOnly( final boolean _httpOnly ) {
        httpOnly = _httpOnly;
    }


    public void setSecure( final boolean _secure ) {
        secure = _secure;
    }


    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }


    public Instant getExpires() {
        return expires;
    }


    public Long getMaxAge() {
        return maxAge;
    }


    public String getDomain() {
        return domain;
    }


    public String getPath() {
        return path;
    }


    public boolean isHttpOnly() {
        return httpOnly;
    }


    public boolean isSecure() {
        return secure;
    }
}
