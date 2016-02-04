package com.slightlyloony.blog.templates;

/**
 * Template elements are the "atomic level" of a template.  Each template element can render some amount of text while the overall template is
 * being rendered.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface TemplateElement {


    /**
     * Returns a {@link TemplateInputStream} that provides the bytes in this instance in an input stream.
     *
     * @return the {@link TemplateInputStream} that provides the bytes in this instance
     */
    TemplateInputStream inputStream();
}
