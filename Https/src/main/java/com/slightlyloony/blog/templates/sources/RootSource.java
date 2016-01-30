package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;
import com.slightlyloony.blog.users.User;
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
    protected RootSource( final DatumDefs _data ) {
        super( null, _data );
    }


    /**
     * Adds the data that are common to all root sources.
     *
     * @param _data the superclass' list of datum definitions
     */
    protected static void addCommon( final List<DatumDef> _data ) {

        _data.add( new DatumDef( "timestamp", DateSource.class, _source -> ZonedDateTime.now( Defaults.TIME_ZONE ) ) );
    }


    /**
     * Returns the datum at the given path.  If any of the path parts evaluate incorrectly, then an error message is returned as a string datum.
     * Otherwise, the datum identified by the last part of the path is returned.
     *
     * @param _user the user whose authorities and name determine whether this datum may be accessed
     * @param _path the path to the desired datum
     * @return the datum desired, or an explanatory string datum if there was a problem
     */
    public Datum getDatum( final User _user, final Path _path ) {
        return _path.getDatum( this, _user, _path );
    }


    /**
     * Returns the value at the given hierarchical indices.  Each index other than the last index is the index for the source returned from the
     * preceding index lookup (or the root source in the case of the first index).  The last index looks up the returned value on the last source.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _path the path to the desired value
     * @return the value desired, or an explanatory string value if there was a problem
     */
    public Object getValue( final User _user, final Path _path ) {
        return getDatum( _user, _path ).getValue();
    }
}
