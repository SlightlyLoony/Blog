package com.slightlyloony.blog.templates.sources.data;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates a description of the defs that can be provided by a source.  Instances of this class are generally constructed as a static final
 * instance per Source class.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DataDefs {

    private final Map<String,Integer> byName;
    private final DatumDef[] defs;


    public DataDefs( final List<DatumDef> _data ) {

        byName = Maps.newHashMap();
        defs = _data.toArray( new DatumDef[_data.size()] );

        for( int i = 0; i < defs.length; i++ ) {
            byName.put( defs[i].getName(), i );
        }
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
