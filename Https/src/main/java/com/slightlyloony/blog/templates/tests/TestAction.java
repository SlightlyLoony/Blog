package com.slightlyloony.blog.templates.tests;

import com.slightlyloony.blog.templates.sources.data.Datum;

/**
 * Implemented by template function actions defined in {@link TestDef}.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface TestAction {

    Datum test( final Datum... _arguments );
}
