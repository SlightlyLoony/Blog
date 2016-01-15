package com.slightlyloony.blog.objects;

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


    public String getID() {
        return id;
    }


    @Override
    public String toString() {
        return id;
    }
}
