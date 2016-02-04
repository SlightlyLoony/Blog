package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;

import java.util.Arrays;

/**
 * Implements a special source that can create and modify variables.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class VariableSource extends SourceBase implements Source {

    /**
     * Creates a new instance of this class.  See the class comments for more details.
     */
    public VariableSource() {
        super( null, new DatumDefs() );
    }


    /**
     * Creates the variable with the given name to the given datum.  If the variable does not already exist, it will be created.
     *
     * @param _name the variable name (without the leading dot)
     */
    public void create( final String _name ) {
        if( _name == null )
            throw new HandlerIllegalArgumentException( "Missing name argument" );

        // some setup...
        DatumDef def = new DatumDef( _name  );

        // make sure this is in our definitions, with the right value...
        int index = datumDefs.ensure( def );
        if( index >= data.length )
            data = Arrays.copyOf( data, index + 1 );
    }


    public void set( final int _index, final Datum _value ) {

        if( (_index < 0) || (_index >= data.length) || (_value == null) )
            return;

        data[_index] = _value;
    }
}
