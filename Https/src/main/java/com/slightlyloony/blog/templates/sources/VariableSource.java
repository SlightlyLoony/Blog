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
     * Sets the variable with the given name to the given datum.  If the variable does not already exist, it will be created.
     *
     * @param _name the variable name (without the leading dot)
     * @param _value the value that should be assigned to the variable (a copy will be made)
     */
    public void set( final String _name, final Datum _value ) {

        if( (_name == null) || (_value == null) )
            throw new HandlerIllegalArgumentException( "Missing name or value argument" );

        // some setup...
        DatumDef def = new DatumDef( _name, _value  );

        // make sure this is in our definitions, with the right value...
        int index = datumDefs.ensure( def );
        if( index >= data.length )
            data = Arrays.copyOf( data, index + 1 );
        data[index] = _value;
    }
}
