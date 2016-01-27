package com.slightlyloony.blog.templates.sources;

import com.slightlyloony.blog.templates.sources.data.Datum;
import com.slightlyloony.blog.users.User;

/**
 * Implemented by template data sources.  A template data source is conceptually a container of named datum items, each of which is an arbitrary
 * object whose toString() method will produce a string that may be used to substitute for a variable in a template, <i>or</i> another source.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Source extends Datum {


    /**
     * Returns the datum with the given name, or null if none exists by that name, or if the datum has no value.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _name the name of the datum to retrieve
     * @return the value of the datum, or null if none exists by that name, or if the datum has no value
     */
    Datum get( final User _user, final String _name );


    /**
     * Returns the datum at the given index, or null if the index is out of range, or if the datum has no value.
     *
     * @param _user the user whose authorities and name determine whether this value may be accessed
     * @param _index the name of the datum to retrieve
     * @return the value of the datum, or null if the index is out of range, or if the datum has no value
     */
    Datum get( final User _user, final int _index );


    /**
     * Returns true if this source has a datum with the given name.  Note that the value of the datum could still be null; this method just
     * checks to see if the name is valid.
     *
     * @param _name the name to check
     * @return true if the name is valid
     */
    boolean has( final String _name );


    /**
     * Returns true if this source has a datum at the given index.  Note that the value of the datum could still be null; this method just
     * checks to see if the index is valid.
     *
     * @param _index the index to check
     * @return true if the index is valid
     */
    boolean has( final int _index );


    /**
     * Returns the index associated with the given name, or -1 if the name does not exist.
     *
     * @param _name the name to get an index for
     * @return the index associated with the given name
     */
    int indexOf( final String _name );


    /**
     * Returns the name associated with the given index, or null if the index does not exist.
     *
     * @param _index the index to get a name for
     * @return the name associated with the given index
     */
    String nameOf( final int _index );
}
