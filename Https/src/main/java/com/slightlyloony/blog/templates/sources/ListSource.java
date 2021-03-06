package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.templates.sources.data.*;

import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ListSource extends SourceBase implements Source {

    private int index;
    private List<Source> sources;


    /**
     * Creates a new instance of this class.  See the class comments for more details.
     *
     * @param _sources the source data for this instance
     */
    public ListSource( final List<Source> _sources ) {
        super( _sources, getDatumDefs( _sources ) );

        index = 0;
        sources = _sources;
    }


    public int size() {
        return sources.size();
    }


    public void reset() {
        index = 0;
    }


    public void inc() {
        index++;
    }


    public int index() {
        return index;
    }


    public Datum resolveSpecialVariables( final String _name ) {

        switch( _name ) {
            case "list_index": return new IntegerDatum( index                       );
            case "list_size":  return new IntegerDatum( sources.size()              );
            case "list_first": return new BooleanDatum( index == 0                  );
            case "list_last":  return new BooleanDatum( index >= sources.size() - 1 );
            default:           return null;
        }
    }


    /**
     * Returns the datum at the given index, or null if the index is out of range, or if the datum has no value.
     *
     * @param _index the name of the datum to retrieve
     * @return the value of the datum, or null if the index is out of range, or if the datum has no value
     */
    @Override
    public Datum get( final int _index ) {
        return sources.get( index ).get( _index );
    }


    private static DatumDefs getDatumDefs( final List<Source> _sources ) {

        if( (_sources == null) || (_sources.size() == 0) )
            throw new HandlerIllegalArgumentException( "Source list for ListSource is missing or empty" );

        // add the index to our datum definitions...
        List<DatumDef> newDefs = Lists.newArrayList();
        newDefs.add( new DatumDef( "index", IntegerDatum.class, _source -> ((ListSource) _source).index() ) );
        return _sources.get( 0 ).getDefs();
    }
}
