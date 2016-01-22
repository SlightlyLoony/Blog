package com.slightlyloony.blog.objects;

import com.google.common.base.Objects;
import com.slightlyloony.blog.util.ID;

/**
 * Contains a 10 character string that is guaranteed to be a semantically valid blog ID.  No guarantee is made about whether the ID refers to an
 * actual object on the blog.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogID {

    private final String id;


    private BlogID( final String _id ) {
        id = _id;
    }


    public static BlogID create( final String _id ) {
        return ( (_id != null) && (_id.length() == 10) && ID.isValid( _id )) ? new BlogID( _id ) : null;
    }


    public long asLong() {
        return ID.decode( id );
    }


    @Override
    public boolean equals( final Object o ) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;
        BlogID blogID = (BlogID) o;
        return Objects.equal( id, blogID.id );
    }


    @Override
    public int hashCode() {
        return Objects.hashCode( id );
    }


    public String getID() {
        return id;
    }


    @Override
    public String toString() {
        return id;
    }
}
