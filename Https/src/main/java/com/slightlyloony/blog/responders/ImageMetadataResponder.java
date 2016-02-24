package com.slightlyloony.blog.responders;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.events.EventType;
import com.slightlyloony.blog.events.Events;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.objects.BlogObjectMetadata;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import static com.slightlyloony.blog.objects.BlogObjectType.METADATA;
import static com.slightlyloony.blog.objects.ContentCompressionState.UNCOMPRESSED;

/**
 * Handles the response to an image metadata request.  The request is JSON object with a single property ("images"), which should be an array of
 * image metadata record blog IDs.  The response is always a JSON object with a property named "images" and a value with a property for each image
 * metadata blog ID.  The value of those properties is the metadata for that image (see example in the code comments below).
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ImageMetadataResponder implements Responder {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Handles the response to a user login request.
     *
     * @param _request the blog request object for this request
     * @param _response the blog response object for this request
     * @param _metadata the metadata for this request
     * @param _isCacheable true if this request is cacheable
     * @throws StorageException on any problem
     */
    @Override
    public void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata, final boolean _isCacheable )
            throws StorageException {

        try {
            // decode the request...
            String json = S.fromUTF8( _request.getPostData() );
            ImageMetadataRequest req = new Gson().fromJson( json, ImageMetadataRequest.class );
            LOG.info( "Got image metadata request for: " + String.join( ", ", req.images ) );

            // gather our information...
            ImageMetadataResponse response = new ImageMetadataResponse();
            for( String image : req.images ) {
                BlogID id = BlogID.create( image.substring( 0, 10 ) );
                BlogObjectAccessRequirements ar = BlogObjectAccessRequirements.get( image.charAt( 10 ) );
                BlogObjectMetadata metadata = (BlogObjectMetadata) BlogServer.STORAGE.read( id, METADATA, ar, UNCOMPRESSED, true );
                ImageMetadata im = new ImageMetadata();
                im.height            = metadata.getHeight();
                im.width             = metadata.getWidth();
                im.size              = metadata.getSize();
                im.source            = metadata.getSource();
                im.credit            = metadata.getCredit();
                im.description       = metadata.getDescription();
                im.title             = metadata.getTitle();
                im.where             = metadata.getWhere();
                im.when              = metadata.getWhen();
                im.cameraSettings    = metadata.getCameraSettings();
                im.cameraOrientation = metadata.getCameraOrientation();
                response.images.put( image, im );
            }

            // and return it...
            _response.sendJSONResponse( new Gson().toJson( response ) );

            // fire success event...
            Events.fire( EventType.IMAGE_META_REQUEST, response.images.size() );

            _request.handled();
        }
        catch( IOException e ) {
            throw new StorageException( "Problem responding to image metadata request: " + e.getMessage(), e );
        }
    }

    private static class ImageMetadataRequest {
        private String[] images;
    }


    private static class ImageMetadataResponse {
        private Map<String, ImageMetadata> images = Maps.newHashMap();
    }


    private static class ImageMetadata {
        private int height;
        private int width;
        private int size;
        private String source;
        private String credit;
        private String description;
        private String title;
        private String where;
        private String when;
        private String cameraSettings;
        private String cameraOrientation;
    }
}

/*

 Sample request:

    {"images":["AAAAAAAAAA","AAAAAAAAAB"]}

 Sample response:
    {"images": {
        "AAAAAAAAAA": {"height":1024,"width":2048,"size":140000,"source":"unknown","title":"something","when":"January 2004"},
        "AAAAAAAAAB": {"height":1024,"width":2048,"size":155500,"source":"oracle","title":"or other","when":"January 2005"}
        }
    }
 */