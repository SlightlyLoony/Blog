package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.DatumBase;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;
import com.slightlyloony.blog.templates.sources.data.StringDatum;
import com.slightlyloony.blog.util.Defaults;

/**
 * The base class that all Source implementations extend.  There is one important feature to note in the design of this class: the DatumDefs object that
 * is the sole argument to the constructor.  These DatumDefs objects are constructed statically when each subclass is first instantiated.  Because
 * constructing that object is relatively expensive, we do it exactly once per execution of the JVM, and then squirrel away the result in the DatumDefs
 * object.  This makes every subsequent construction of the subclass a very light-weight affair, both in terms of memory consumption and CPU
 * consumption.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class SourceBase extends DatumBase implements Source {

    protected final DatumDefs datumDefs;
    protected Datum[] data;


    /**
     * Creates a new instance of this class.  See the class comments for more details.
     *
     * @param _value the value of this object, if referenced directly
     * @param _data the DatumDefs object containing the structure of this instance, as defined by the subclass
     */
    protected SourceBase( final Object _value, final DatumDefs _data) {
        super( _value );
        datumDefs = _data;
        data = new Datum[datumDefs.size()];
    }


    /**
     * Returns the datum at the given index, or null if the index is out of range, or if the datum has no value.
     *
     * @param _index the name of the datum to retrieve
     * @return the value of the datum, or null if the index is out of range, or if the datum has no value
     */
    @Override
    public Datum get( final int _index ) {

        if( (_index < 0) || (_index >= data.length) )
            return null;

        if( datumDefs.get( _index ).isAuthorized( this )) {

            Datum datum = data[_index];
            if( datum == null )
                data[_index] = datum = datumDefs.get( _index ).getDatum( this );

            return datum;
        }
        else
            return new StringDatum( Defaults.REDACTED );
    }


    /**
     * Returns this source's datum definitions.
     *
     * @return this source's datum definitions
     */
    public DatumDefs getDefs() {
        return datumDefs;
    }
}
