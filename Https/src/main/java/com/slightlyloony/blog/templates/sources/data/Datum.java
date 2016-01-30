package com.slightlyloony.blog.templates.sources.data;

/**
 * Implemented by classes that provide an individual datum for sources.  A datum may come from storage inside the instance, or it may call out
 * somewhere to get it.  A datum has a name, an expected type, and (of course) a way to get the value of the datum.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Datum {


    /**
     * Returns the value of this datum.
     *
     * @return a getter for the value of this datum
     */
    Object getValue();
}
