package com.slightlyloony.blog.templates.sources.data;

import com.google.common.collect.Maps;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates a description of the defs that can be provided by a source.  Instances of this class are generally constructed as a static final
 * instance per Source class.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DatumDefs {

    private final Map<String,Integer> byName;
    private DatumDef[] defs;


    public DatumDefs() {
        byName = Maps.newHashMap();
        defs = new DatumDef[0];
    }

    public DatumDefs( final List<DatumDef> _data ) {

        byName = Maps.newHashMap();
        defs = _data.toArray( new DatumDef[_data.size()] );

        for( int i = 0; i < defs.length; i++ ) {
            byName.put( defs[i].getName(), i );
        }
    }


    /**
     * Ensures that the given datum definition is in this class and is updated to the given value.  This method should be used ONLY by a variable
     * source.
     *
     * @param _datumDef the datum definition to add or update
     * @return the index of the definition
     */
    public int ensure( final DatumDef _datumDef ) {

        if( _datumDef == null )
            throw new HandlerIllegalArgumentException( "Missing required DatumDef argument" );

        Integer index = byName.get( _datumDef.getName() );

        if( index != null ) {
            defs[index] = _datumDef;
        }
        else {
            index = defs.length;
            defs = Arrays.copyOf( defs, defs.length + 1 );
            defs[index] = _datumDef;
            byName.put( _datumDef.getName(), index );
        }
        return index;
    }


    public DatumDef get( final int _index ) {
        return defs[_index];
    }


    public Integer byName( final String _name ) {
        return byName.get( _name );
    }


    public boolean containsName( final String _name ) {
        return byName.containsKey( _name );
    }


    public int size() {
        return defs.length;
    }


    public String getName( final int _index ) {
        return defs[_index].getName();
    }
}
