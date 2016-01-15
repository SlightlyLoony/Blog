package com.slightlyloony.blog.util;

import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;

/**
 * Represents any valid URI on the blog.  Successful creation of an instance of this class guarantees that the URI is in the correct form and that all
 * the characters are valid.  Provides convenience methods for decoding the URI.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogURI {

    private final String rawURI;
    private final BlogID id;
    private final BlogObjectAccessRequirements rights;


    private BlogURI( final String _rawURI, final BlogID _id, final BlogObjectAccessRequirements _rights ) {
        rawURI = _rawURI;
        id = _id;
        rights = _rights;
    }


    public static BlogURI create( final String _rawURI ) {

        if( (_rawURI == null) || !_rawURI.startsWith( "/" ) || (_rawURI.length() != 12) )
            return null;

        BlogID id = BlogID.create( _rawURI.substring( 1, 11 ) );
        if( id == null )
            return null;

        BlogObjectAccessRequirements rights = BlogObjectAccessRequirements.get( _rawURI.charAt( 11 ) );
        if( rights == null )
            return null;

        return new BlogURI( _rawURI, id, rights );
    }


    public String getRawURI() {
        return rawURI;
    }


    public BlogID getId() {
        return id;
    }


    public BlogObjectAccessRequirements getRightsRequired() {
        return rights;
    }
}
