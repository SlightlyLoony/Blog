package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IntegerDatum extends DatumBase {

    public IntegerDatum( final String _name, final Integer _value ) {
        super( _name, Integer.class, _value );
    }


    /**
     * Returns a copy of this datum with the new given name.
     *
     * @param _name the name for the copy
     * @return the datum copy
     */
    @Override
    public Datum copy( final String _name ) {
        return new IntegerDatum( _name, (Integer) value );
    }
}
