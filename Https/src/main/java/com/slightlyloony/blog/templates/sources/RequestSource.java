package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;

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

//        result[0] = new StringDatum( "method", _request.getRequestMethod().name() );
//        result[1] = new StringDatum( "id",     _request.getId().getID()           );
//        result[2] = new StringDatum( "blog",   _request.getBlog().getName()       );

        return new DatumDefs( result );
    }
}
