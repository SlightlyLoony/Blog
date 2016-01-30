package com.slightlyloony.blog.templates.sources.data;

/**
 * Base class for all datum subclasses  A datum has a name, an expected type, and a getter for the piece of information (or a way to get it) being
 * provided.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class DatumBase implements Datum {

    protected final Object value;

    protected DatumBase( final Object _value ) {
        value = _value;
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
