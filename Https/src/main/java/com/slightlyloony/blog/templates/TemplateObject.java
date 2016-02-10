package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.storage.StorageInputStream;

import java.io.IOException;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TemplateObject extends BlogContentObject {

    private Template template;
    private TemplateInputStream templateInputStream;


    public TemplateObject( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements,
                           final Template _template ) {
        super( _id, _type, _accessRequirements, getContent( _template ) );
        template = _template;
    }


    @Override
    public synchronized StorageInputStream getStream() {
        TemplateInputStream is = template.inputStream();
        is.reset();
        return new StorageInputStream( is, null );
    }


    public void reset() {
        try {
            ( (StreamObjectContent) content ).getStream().reset();
        }
        catch( IOException e ) {
            throw new HandlerIllegalStateException( "Should not be possible for TemplateInputStream to get IOException" );
        }
    }


    /**
     * The approximate memory requirements for this instance, in bytes.
     *
     * @return the approximate number of bytes this instance occupies in memory
     */
    @Override
    public int size() {
        return 8 + template.size();
    }


    private static BlogObjectContent getContent( final Template _template ) {

        if( _template == null )
            throw new HandlerIllegalArgumentException( "Missing required template" );

        return new StreamObjectContent( new StorageInputStream( _template.inputStream(), null), ContentCompressionState.DO_NOT_COMPRESS );
    }
}
