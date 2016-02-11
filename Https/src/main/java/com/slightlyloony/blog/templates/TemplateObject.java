package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.storage.StorageInputStream;

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
        ((TemplateObjectContent) content).setTemplate( template );
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

        return new TemplateObjectContent( new StorageInputStream( _template.inputStream(), null), ContentCompressionState.DO_NOT_COMPRESS );
    }


    /**
     * Extends {@link StreamObjectContent} to override {@link StreamObjectContent#getStream()}, to get a new stream object every time it's called.
     */
    private static class TemplateObjectContent extends StreamObjectContent {

        private Template template;

        public TemplateObjectContent( final StorageInputStream _content, final ContentCompressionState _compressionState ) {
            super( _content, _compressionState );
        }


        public void setTemplate( final Template _template ) {
            template = _template;
        }


        @Override
        public StorageInputStream getStream() {
            return new StorageInputStream( template.inputStream(), null);
        }
    }
}
