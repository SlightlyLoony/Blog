package com.slightlyloony.blog.templates.sources.data;

/**
 * Base class for all datum subclasses  A datum has a name, an expected type, and a getter for the piece of information (or a way to get it) being
 * provided.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class DatumBase implements Datum {

    protected final String name;
    protected final Class type;
    protected final Object value;

    protected DatumBase( final String _name, final Class _type, final Object _value ) {
        name = _name;
        type = _type;
        value = _value;
    }


    /**
     * Returns the name of this datum.
     *
     * @return the name of this datum
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the class object representing the expected type of this datum.
     *
     * @return the class object representing the expected type of this datum
     */
    public Class getExpectedType() {
        return type;
    }


    /**
     * Returns a getter for the value of this datum.
     *
     * @return a getter for the value of this datum
     */
    public Object getValue() {
        return value;
    }
}
