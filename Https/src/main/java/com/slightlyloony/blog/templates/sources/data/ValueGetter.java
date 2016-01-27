package com.slightlyloony.blog.templates.sources.data;

import com.slightlyloony.blog.templates.sources.Source;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface ValueGetter {

    public Object get( final Source _source );
}
