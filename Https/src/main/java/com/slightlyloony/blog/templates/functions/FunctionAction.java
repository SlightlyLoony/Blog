package com.slightlyloony.blog.templates.functions;

import com.slightlyloony.blog.templates.sources.data.Datum;

/**
 * Implemented by template function actions defined in {@link FunctionDef}.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface FunctionAction {

    Datum action( final Datum... _arguments );
}
