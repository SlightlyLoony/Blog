package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BooleanDatum extends DatumBase {

    public BooleanDatum( final String _name, final Boolean _value ) {
        super( _name, Boolean.class, _value );
    }


    /**
     * Returns a copy of this datum with the new given name.
     *
     * @param _name the name for the copy
     * @return the datum copy
     */
    @Override
    public Datum copy( final String _name ) {
        return new BooleanDatum( _name, (Boolean) value );
    }
}
