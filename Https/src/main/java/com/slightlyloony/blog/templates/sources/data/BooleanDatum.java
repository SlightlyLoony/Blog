package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BooleanDatum extends DatumBase {

    public BooleanDatum( final String _name, final Boolean _value ) {
        super( _name, Boolean.class, _value );
    }
}
