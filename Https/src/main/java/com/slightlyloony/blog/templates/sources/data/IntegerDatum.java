package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IntegerDatum extends DatumBase {

    public IntegerDatum( final String _name, final Integer _value ) {
        super( _name, Integer.class, _value );
    }
}
