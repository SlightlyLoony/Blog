package com.slightlyloony.blog.security;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogUserRights {

    public Set<BlogAccessRight> rights;

    public BlogUserRights() {
        rights = Sets.newHashSet();
    }


    public void add( final BlogAccessRight _right ) {

        if( _right == null )
            throw new IllegalArgumentException( "Missing the BlogAccessRight to add" );

        rights.add( _right );
    }


    public boolean has( final BlogAccessRight _right ) {

        if( _right == null )
            throw new IllegalArgumentException( "Missing the BlogAccessRight to test" );

        return rights.contains( _right );
    }
}
