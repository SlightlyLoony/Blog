package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.templates.sources.data.DataDefs;
import com.slightlyloony.blog.templates.sources.data.DatumDef;

import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RequestSource extends SourceBase implements Source {

    private static final DataDefs DATA_DEFS = getData();

    protected RequestSource( final String _name, final BlogRequest _value ) {
        super( _name, BlogRequest.class, _value, DATA_DEFS );
    }


    private static DataDefs getData() {

        List<DatumDef> result = Lists.newArrayList();

//        result[0] = new StringDatum( "method", _request.getRequestMethod().name() );
//        result[1] = new StringDatum( "id",     _request.getId().getID()           );
//        result[2] = new StringDatum( "blog",   _request.getBlog().getName()       );

        return new DataDefs( result );
    }
}
