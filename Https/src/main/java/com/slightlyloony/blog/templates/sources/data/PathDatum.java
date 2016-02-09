package com.slightlyloony.blog.templates.sources.data;

import com.slightlyloony.blog.templates.sources.Path;

/**
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class PathDatum extends DatumBase implements Datum {

    public PathDatum( final Path _path ) {
        super( _path );
    }


    /**
     * Returns the value of this datum.
     *
     * @return the value of this datum
     */
    @Override
    public Object getValue() {
        return ((Path) value).getDatum().getValue();
    }


    public Path getPath() {
        return (Path) value;
    }
}
