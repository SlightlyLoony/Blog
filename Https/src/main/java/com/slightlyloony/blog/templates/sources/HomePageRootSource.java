package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;

import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HomePageRootSource extends RootSource {

    /**
     * Create a new instance of this class with the given sources (or data).
     */
    public HomePageRootSource( final BlogRequest _request ) {
        super( getData( _request ) );
    }


    private static DatumDefs getData( final BlogRequest _request ) {

        List<DatumDef> sources = Lists.newArrayList();
        RootSource.addCommon( sources );
        sources.add( new DatumDef( "request", RequestSource.class, _source -> _request           ) );
        sources.add( new DatumDef( "user",    UserSource.class,    _source -> _request.getUser() ) );
        sources.add( new DatumDef( "blog",    BlogSource.class,    _source -> _request.getBlog() ) );

        return new DatumDefs( sources );
    }
}
