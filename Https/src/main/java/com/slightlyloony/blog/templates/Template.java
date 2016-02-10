package com.slightlyloony.blog.templates;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Template implements TemplateElement {

    private TemplateElements elements;


    public Template(  final TemplateElements _elements  ) {
        elements = _elements;
    }


    /**
     * Returns a {@link TemplateInputStream} that provides the bytes in this instance in an input stream.
     *
     * @return the {@link TemplateInputStream} that provides the bytes in this instance
     */
    @Override
    public TemplateInputStream inputStream() {
        return elements.inputStream();
    }


    /**
     * Returns an estimate of the memory size of this object, in bytes.
     *
     * @return the estimated number of bytes of this object
     */
    @Override
    public int size() {
        return elements.size();
    }
}
