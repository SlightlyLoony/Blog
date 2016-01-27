package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StringDatum extends DatumBase {

    public StringDatum( final String _name, final String _value ) {
        super( _name, String.class, _value );
    }
}
