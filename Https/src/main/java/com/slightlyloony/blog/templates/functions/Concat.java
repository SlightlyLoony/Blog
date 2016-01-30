package com.slightlyloony.blog.templates.functions;

import com.slightlyloony.blog.templates.TemplateUtil;
import com.slightlyloony.blog.templates.sources.data.Datum;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Concat implements Function {

    private Datum[] inputs;


    public Concat( final Datum[] _inputs ) {
        inputs = _inputs;
    }


    /**
     * Returns the value of this datum.
     *
     * @return a getter for the value of this datum
     */
    @Override
    public Object getValue() {

        StringBuilder sb = new StringBuilder();
        for( Datum datum : inputs ) {
            sb.append( TemplateUtil.toString( datum.getValue() ) );
        }
        return sb.toString();
    }
}
