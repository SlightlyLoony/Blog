package com.slightlyloony.blog.templates.sources.data;

import com.slightlyloony.blog.templates.Template;
import com.slightlyloony.blog.templates.sources.Path;

/**
 * Implements a datum used to represent a value that is extracted from a source at runtime.  This effectively binds a template (which provides source
 * and user) and path to provide access to a datum value.  These are constructed by the compiler and are basically invisible to the template writer.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SourceDatum extends DatumBase implements Datum {

    private final Template template;
    private final Path path;


    public SourceDatum( final Template _template, final Path _path ) {
        super( null );

        template = _template;
        path = _path;
    }


    @Override
    public Object getValue() {
        return path.getDatum( template.getSource(), template.getUser() ).getValue();
    }
}
