package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;

/**
 * Implemented by template data sources.  A template data source is conceptually a container of named datum items, each of which is an arbitrary
 * object whose toString() method will produce a string that may be used to substitute for a variable in a template, <i>or</i> another source.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Source extends Datum {


    /**
     * Returns the datum at the given index, or null if the index is out of range, or if the datum has no value.
     *
     * @param _index the name of the datum to retrieve
     * @return the value of the datum, or null if the index is out of range, or if the datum has no value
     */
    Datum get( final int _index );


    /**
     * Returns this source's datum definitions.
     *
     * @return this source's datum definitions
     */
    public DatumDefs getDefs();
}
