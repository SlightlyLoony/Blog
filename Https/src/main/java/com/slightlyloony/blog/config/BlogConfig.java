package com.slightlyloony.blog.config;

import java.util.Map;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogConfig {

    Map<String,String> mappings;


    public String map( final String _path ) {
        return mappings.getOrDefault( _path, _path );
    }
}
