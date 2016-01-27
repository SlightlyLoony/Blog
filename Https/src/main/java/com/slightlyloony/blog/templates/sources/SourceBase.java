package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.DataDefs;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.DatumBase;
import com.slightlyloony.blog.templates.sources.data.StringDatum;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.util.Defaults;

/**
 * The base class that all Source implementations extend.  There is one important feature to note in the design of this class: the DataDefs object that
 * is the sole argument to the constructor.  These DataDefs objects are constructed statically when each subclass is first instantiated.  Because
 * constructing that object is relatively expensive, we do it exactly once per execution of the JVM, and then squirrel away the result in the DataDefs
 * object.  This makes every subsequent construction of the subclass a very light-weight affair, both in terms of memory consumption and CPU
 * consumption.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class SourceBase extends DatumBase implements Source {

    private final DataDefs dataDefs;
    private final Datum[] data;


    /**
     * Creates a new instance of this class.  See the class comments for more details.
     *
     * @param _data the DataDefs object containing the structure of this instance, as defined by the subclass
     */
    protected SourceBase( final String _name, final Class _class, final Object _value, final DataDefs _data) {
        super( _name, _class, _value );
        dataDefs = _data;
        data = new Datum[dataDefs.size()];
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

        String[] parts = _name.split( "\\." );

        Datum value = this;
        Source source = this;
        String lastPart = "";

        for( String part : parts ) {

            if( !(value instanceof Source) )
                return "<<non-source: " + lastPart + ">>";

            source = (Source) value;

            if( !source.has( part ) )
                return "<<undefined: " + part + ">>";

            Datum newValue = source.get( _user, part );

            if( newValue == null )
                return "<<empty: " + part + ">>";

            lastPart = part;
            value = newValue;
        }
        return value.getValue();
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

        Datum value = this;
        Source source = this;

        for( Integer index : _indices ) {

            if( !(value instanceof Source) )
                return "<<non-source: " + source.getName() + ">>";

            source = (Source) value;

            if( !source.has( index ) )
                return "<<undefined: " + source.getName() + ">>";

            Datum newValue = source.get( _user, index );

            if( newValue == null )
                return "<<empty: " + source.getName() + ">>";

            value = newValue;
        }
        return value.getValue();
    }


    /**
     * Returns the datum with the given name, or null if none exists by that name, or if the datum has no value.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _name the name of the datum to retrieve
     * @return the value of the datum, or null if none exists by that name, or if the datum has no value
     */
    @Override
    public Datum get( final User _user, final String _name ) {

        if( _name == null )
            return null;

        Integer index = dataDefs.byName( _name );
        if( index == null )
            return null;

        return get( _user, index );
    }


    /**
     * Returns the datum at the given index, or null if the index is out of range, or if the datum has no value.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _index the name of the datum to retrieve
     * @return the value of the datum, or null if the index is out of range, or if the datum has no value
     */
    @Override
    public Datum get( final User _user, final int _index ) {

        if( (_index < 0) || (_index >= data.length) )
            return null;

        if( dataDefs.get( _index ).isAuthorized( _user, this )) {

            Datum datum = data[_index];
            if( datum == null )
                data[_index] = datum = dataDefs.get( _index ).getDatum( this );

            return datum;
        }
        else
            return new StringDatum( dataDefs.getName( _index ), Defaults.REDACTED );
    }


    /**
     * Returns true if this source has a datum with the given name.  Note that the value of the datum could still be null; this method just checks to
     * see if the name is valid.
     *
     * @param _name the name to check
     * @return true if the name is valid
     */
    @Override
    public boolean has( final String _name ) {
        return (_name != null) && dataDefs.containsName( _name );
    }


    /**
     * Returns true if this source has a datum at the given index.  Note that the value of the datum could still be null; this method just checks to
     * see if the index is valid.
     *
     * @param _index the index to check
     * @return true if the index is valid
     */
    @Override
    public boolean has( final int _index ) {
        return (_index >= 0) && (_index < dataDefs.size());
    }


    /**
     * Returns the index associated with the given name, or -1 if the name does not exist.
     *
     * @param _name the name to get an index for
     * @return the index associated with the given name
     */
    @Override
    public int indexOf( final String _name ) {

        if( _name == null )
            return -1;

        Integer index = dataDefs.byName( _name );
        if( index == null )
            return -1;

        return index;
    }


    /**
     * Returns the name associated with the given index, or null if the index does not exist.
     *
     * @param _index the index to get a name for
     * @return the name associated with the given index
     */
    @Override
    public String nameOf( final int _index ) {
        return ( (_index < 0) || (_index >= dataDefs.size()) ) ? null : dataDefs.getName( _index );
    }
}
