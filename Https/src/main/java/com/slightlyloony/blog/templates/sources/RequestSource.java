package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;
import com.slightlyloony.blog.templates.sources.data.StringDatum;

import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RequestSource extends SourceBase implements Source {

    private static final DatumDefs DATA_DEFS = getData();

    protected RequestSource( final BlogRequest _value ) {
        super( _value, DATA_DEFS );
    }


    private static DatumDefs getData() {

        List<DatumDef> result = Lists.newArrayList();

        result.add( new DatumDef( "method", StringDatum.class, RequestSource::getRequestMethod ) );
        result.add( new DatumDef( "id",     StringDatum.class, RequestSource::getID            ) );
        result.add( new DatumDef( "blog",   StringDatum.class, RequestSource::getBlog          ) );

        return new DatumDefs( result );
    }


    private static String getRequestMethod( final Source _source ) {
        return request( _source ).getRequestMethod().name();
    }


    private static String getBlog( final Source _source ) {
        return request( _source ).getBlog().getName();
    }


    private static String getID( final Source _source ) {
        return request( _source ).getId().getID();
    }


    private static BlogRequest request( final Source _source ) {
        return (BlogRequest) ((RequestSource) _source).getValue();
    }
}
