package com.slightlyloony.blog.templates.sources.data;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class PlaceholderDatum extends DatumBase implements Datum {

    private Datum datum;


    public PlaceholderDatum() {
        super( "PLACEHOLDER" );
    }


    public void setValue( final Datum _datum ) {
        datum = _datum;
    }


    @Override
    public Object getValue() {
        return datum.getValue();
    }
}
