package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.DataDefs;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.util.Defaults;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Instances of this class are the root source suitable for use as the data source input to a template.  Each context in which a root source is
 * needed extends this class.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class RootSource extends SourceBase implements Source {

    /**
     * Create a new instance of this class with the given sources (or data).
     *
     */
    protected RootSource( final DataDefs _data ) {
        super( null, null, null, _data );
    }


    /**
     * Adds the data that are common to all root sources.
     *
     * @param _data
     */
    protected static void addCommon( final List<DatumDef> _data ) {

        _data.add( new DatumDef( "timestamp", DateSource.class, _source -> ZonedDateTime.now( Defaults.TIME_ZONE ) ) );
    }
}
