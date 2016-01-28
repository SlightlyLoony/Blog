package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StringDatum extends DatumBase {

    public StringDatum( final String _name, final String _value ) {
        super( _name, String.class, _value );
    }


    /**
     * Returns a copy of this datum with the new given name.
     *
     * @param _name the name for the copy
     * @return the datum copy
     */
    @Override
    public Datum copy( final String _name ) {
        return new StringDatum( _name, (String) value );
    }
}
