package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.handlers.BlogRequest;

/**
 * Enumerates all the types of template sources, and provides a factory method to generate them.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum SourceType {

    Home( SourceType::getHomePageSource );


    private SourceGetter getter;


    SourceType( final SourceGetter _getter ) {
        getter = _getter;
    }


    private static RootSource getHomePageSource( final BlogRequest _request ) {
        return new HomePageRootSource( _request );
    }


    public RootSource getSource( final BlogRequest _request ) {
        return getter.getSource( _request );
    }


    private static interface SourceGetter {
        public RootSource getSource( final BlogRequest _request );
    }
}
