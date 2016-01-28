package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.DatumDefs;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.StringDatum;
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


    /**
     * Returns the datum at the given source name.  The name may include periods to separate names that traverse a hierarchy.  For example, the name
     * "request.user.firstname" might refer to the "firstname" datum in source found in the "user" datum of the "request" source.  If any of the name
     * parts evaluate incorrectly, then an error message is returned.  Otherwise, the datum returned by the last name is returned.
     *
     * @param _user the user whose authorities and name determine whether this datum may be accessed
     * @param _name the possibly dot-separated name of the datum desired
     * @return the datum desired, or an explanatory string datum if there was a problem
     */
    public Datum getDatum( final User _user, final String _name ) {

        String[] parts = _name.split( "\\." );

        Datum value = this;
        Source source = this;
        String lastPart = "";

        for( String part : parts ) {

            if( !(value instanceof Source) )
                return new StringDatum( "", "<<non-source: " + lastPart + ">>" );

            source = (Source) value;

            if( !source.has( part ) )
                return new StringDatum( "", "<<undefined: " + part + ">>" );

            Datum newValue = source.get( _user, part );

            if( newValue == null )
                return new StringDatum( "", "<<empty: " + part + ">>" );

            lastPart = part;
            value = newValue;
        }
        return value;
    }


    /**
     * Returns the datum at the given hierarchical indices.  Each index other than the last index is the index for the source returned from the
     * preceding index lookup (or the root source in the case of the first index).  The last index looks up the returned datum on the last source.
     *
     * @param _user the user whose authorities and name determine whether this datum may be accessed
     * @param _indices the hierarchical indices for looking up a datum
     * @return the datum desired, or an explanatory string datum if there was a problem
     */
    public Datum getDatum( final User _user, final Integer... _indices ) {

        Datum value = this;
        Source source = this;

        for( Integer index : _indices ) {

            if( !(value instanceof Source) )
                return new StringDatum( "", "<<non-source: " + source.getName() + ">>" );

            source = (Source) value;

            if( !source.has( index ) )
                return new StringDatum( "", "<<undefined: " + source.getName() + ">>" );

            Datum newValue = source.get( _user, index );

            if( newValue == null )
                return new StringDatum( "", "<<empty: " + source.getName() + ">>" );

            value = newValue;
        }
        return value;
    }


    /**
     * Returns the value at the given source name.  The name may include periods to separate names that traverse a hierarchy.  For example, the name
     * "request.user.firstname" might refer to the "firstname" value in source found in the "user" value of the "request" source.  If any of the name
     * parts evaluate incorrectly, then an error message is returned.  Otherwise, the value returned by the last name is returned.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _name the possibly dot-separated name of the value desired
     * @return the value desired, or an explanatory string value if there was a problem
     */
    public Object getValue( final User _user, final String _name ) {
        return getDatum( _user, _name ).getValue();
    }


    /**
     * Returns the value at the given hierarchical indices.  Each index other than the last index is the index for the source returned from the
     * preceding index lookup (or the root source in the case of the first index).  The last index looks up the returned value on the last source.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _indices the hierarchical indices for looking up a value
     * @return the value desired, or an explanatory string value if there was a problem
     */
    public Object getValue( final User _user, final Integer... _indices ) {
        return getDatum( _user, _indices ).getValue();
    }


    /**
     * Returns a copy of this datum with the new given name.
     *
     * @param _name the name for the copy
     * @return the datum copy
     */
    @Override
    public Datum copy( final String _name ) {
        return null;
    }
}
