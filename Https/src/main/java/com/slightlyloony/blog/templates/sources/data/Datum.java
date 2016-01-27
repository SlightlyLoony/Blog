package com.slightlyloony.blog.templates.sources.data;

/**
 * Implemented by classes that provide an individual datum for sources.  A datum may come from storage inside the instance, or it may call out
 * somewhere to get it.  A datum has a name, an expected type, and (of course) a way to get the value of the datum.  This is done indirectly,
 * through a factory method, in order to provide runtime binding of dynamic sources with static data definitions.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Datum {


    /**
     * Returns the name of this datum.
     *
     * @return the name of this datum
     */
    String getName();


    /**
     * Returns the class object representing the expected type of this datum.
     *
     * @return the class object representing the expected type of this datum
     */
    Class getExpectedType();


    /**
     * Returns the value of this datum.
     *
     * @return a getter for the value of this datum
     */
    Object getValue();
}
