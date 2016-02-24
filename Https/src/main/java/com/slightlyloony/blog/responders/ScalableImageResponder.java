package com.slightlyloony.blog.responders;

import com.google.common.collect.Maps;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.objects.BlogObjectMetadata.ScaledImage;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Handles the response when the content comes from an image that may be presented in different scales.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ScalableImageResponder implements Responder {

    private static Logger LOG = LogManager.getLogger();

    private static Map<String,Semaphore> locks = Maps.newHashMap();


    /**
     * Handles the response to a blog object request.
     *
     * @param _request the blog request object for this request
     * @param _response the blog response object for this request
     * @param _metadata the metadata for this request
     * @param _isCacheable true if this request is cacheable
     * @throws StorageException on any problem
     */
    @Override
    public void respond( final BlogRequest _request, final BlogResponse _response, final BlogObjectMetadata _metadata,
                         final boolean _isCacheable ) throws StorageException {

        BlogID content = _metadata.getContent();
        BlogObjectType contentType = _metadata.getContentType();
        ContentCompressionState compressionState = _metadata.getCompressionState();
        BlogContentObject obj;

        // if we have a height parameter, then we have a scaled request...
        int height = _request.getIntParameter( "h" );
        if( height != 0 ) {

            boolean dirty = false;   // gets set true if we change the metadata and need to write it back out...

            // get a lock so two concurrent threads don't stomp on each other...
            getLock( _metadata.getBlogID() );
            LOG.info( LU.msg( "Got lock for {0}", _metadata.getBlogID().getID() ) );

            try {

                // re-get the metadata while we're in the lock...
                BlogObjectMetadata metadata = (BlogObjectMetadata) BlogServer.STORAGE.read( _metadata.getBlogID(), BlogObjectType.METADATA,
                        _metadata.getAccessRequirements(), compressionState, _isCacheable );

                BufferedImage bi = null;

                // if we don't know our base image's dimensions, then we'd better get them...
                if( (metadata.getHeight() == 0) || (metadata.getWidth() == 0) ) {
                    bi = readBaseImage( metadata, compressionState, _isCacheable );
                    dirty = true;
                }

                // what's the best image we have?
                ScaledImage bestFit = getBestFit( metadata, height );

                int sendingCostDelta = calcSendingCostDelta( metadata, bestFit, height );
                int scalingCost = calcScalingCost( metadata, height );

                // if it costs less to send what we have, do so...
                if( sendingCostDelta < scalingCost ) {
                    obj = (BlogContentObject) BlogServer.STORAGE.read( bestFit.content, contentType, null, compressionState, _isCacheable );
                }

                // otherwise, let's make a new scaled image, store it, and send that...
                else {

                    // read the base image, if we haven't done so already...
                    if( bi == null )
                        bi = readBaseImage( metadata, compressionState, _isCacheable );

                    // scale it appropriately...
                    bi = scale( bi, metadata, height );

                    // write it out to a new file...
                    ByteArrayOutputStream baos = new ByteArrayOutputStream( 1000 );
                    ImageIO.write( bi, metadata.getContentType().name(), baos );
                    byte[] bytes = baos.toByteArray();
                    BlogObjectContent boc = new BytesObjectContent( bytes, compressionState, bytes.length );
                    obj = new BlogContentObject( BlogIDs.INSTANCE.getNextBlogID(), contentType, null, boc );
                    BlogServer.STORAGE.create( obj );

                    // add this new guy to our scaled image data...
                    ScaledImage si = new ScaledImage( obj.getBlogID(), bi.getHeight(), bi.getWidth() );
                    metadata.add( si );
                    dirty = true;
                }

                // if our metadata has been modified, we need to write it back out...
                if( dirty )
                    BlogServer.STORAGE.update( metadata );
            }
            catch( IOException e ) {
                throw new StorageException( "Problem reading image data: " + e.getMessage(), e );
            }
            finally {
                LOG.info( LU.msg( "Releasing lock for {0}", _metadata.getBlogID().getID() ) );
                releaseLock( _metadata.getBlogID() );
            }
        }

        // otherwise, we just return the raw object...
        else {

            // read the file...
            obj = (BlogContentObject) BlogServer.STORAGE.read( content, contentType, null, compressionState, _isCacheable );
        }

        _response.setMimeType( _metadata.getContentType() );

        obj.getContent().write( _request, _response, _metadata.getCompressionState().mayCompress() );
        _request.handled();
    }


    private BufferedImage scale( final BufferedImage _baseImage, final BlogObjectMetadata _metadata, final int _requestedHeight ) {

        // calculate the scale factor and new height and width...
        double sf = 1.0d * _requestedHeight / _metadata.getHeight();
        int rw = (int) Math.floor( sf * _metadata.getWidth() );

        BufferedImage result = new BufferedImage( rw, _requestedHeight, _baseImage.getType() );
        Graphics2D g = result.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance( sf, sf );
        g.drawRenderedImage( _baseImage, at );

        return result;
    }


    private int calcSendingCostDelta( final BlogObjectMetadata _metadata, final ScaledImage _bestFit, final int _requestedHeight ) {

        // estimate number of bytes if we send the best fit...
        double bgBytes = 1.0d * _metadata.getSize() * Math.pow( 1.0d * _bestFit.height / _metadata.getHeight(), 2 );

        // estimate number of bytes if we scale to meet the request...
        double siBytes = 1.0d * _metadata.getSize() * Math.pow( 1.0d * _requestedHeight / _metadata.getHeight(), 2 );

        // now compute the difference in estimated microseconds (assuming 10 mbps)...
        return (int) Math.round(bgBytes - siBytes);  // each byte is about 10 bits on the wire, so each byte is a microsecond, more or less...
    }


    private int calcScalingCost( final BlogObjectMetadata _metadata, final int _requestedHeight ) {

        // some basic numbers...
        double r = 1.0d * _metadata.getHeight() / _requestedHeight;  // always >= 1.0
        double i = Math.floor( r );
        double f = r - i;
        int rw = (int) Math.ceil( 1.0d * _requestedHeight * _metadata.getWidth() / _metadata.getHeight() );
        int t = _requestedHeight * rw;

        // calculate the number of pixels we have to touch in the source per pixel in the destination...
        double p = 2*i*f*f + f*f + 2*i*i*f + i*i;

        // now total the touched pixels, times a guesstimate of the cost (in microseconds) per pixel touched...
        return (int) Math.round( p * t * 0.01d );
    }


    private ScaledImage getBestFit( final BlogObjectMetadata _metadata, final int _requestedHeight ) {

        // if we don't have any scaled images, the biggest scaled image is too small,
        // or if the requested height is larger than the base image's, return the base image...
        ScaledImage[] scaledImages = _metadata.getScaledImages();
        if( (scaledImages == null) || (scaledImages.length == 0) || (scaledImages[scaledImages.length - 1].height < _requestedHeight)
                || (_metadata.getHeight() < _requestedHeight) )

            return new ScaledImage( _metadata.getContent(), _metadata.getHeight(), _metadata.getWidth() );

        // otherwise, cycle through the scaled images until we find the first one bigger than 90% of the requested height...
        // the "90%" allows a teensy bit of upscaling on the client, not enough (we hope!) to cause visible artifacts...
        for( ScaledImage scaledImage : scaledImages ) {
            if( scaledImage.height >= 0.9d * _requestedHeight )
                return scaledImage;
        }

        // it should be impossible to get here...
        return new ScaledImage( _metadata.getContent(), _metadata.getHeight(), _metadata.getWidth() );
    }


    private BufferedImage readBaseImage( final BlogObjectMetadata _metadata, final ContentCompressionState _compressionState,
                                         final boolean _isCacheable ) throws StorageException, IOException {

        BlogContentObject obj = (BlogContentObject)
                BlogServer.STORAGE.read( _metadata.getContent(), _metadata.getContentType(), null, _compressionState, _isCacheable );

        BufferedImage bi = ImageIO.read( obj.getStream() );
        _metadata.setHeight( bi.getHeight() );
        _metadata.setWidth( bi.getWidth() );
        _metadata.setSize( obj.contentLength() );
        return bi;
    }


    /**
     * Blocks until an exclusive lock is obtained for the given blog ID.
     *
     * @param _id the blog ID to obtain a lock for
     */
    private void getLock( final BlogID _id ) {

        // get or make our lock...
        Semaphore semaphore;
        synchronized( locks ) {
            semaphore = locks.get( _id.getID() );
            if( semaphore == null ) {
                semaphore = new Semaphore( 1 );
                locks.put( _id.getID(), semaphore );
            }
        }

        // block until (and if!) we get a permit...
        semaphore.acquireUninterruptibly();
    }


    /**
     * Releases a previously obtained exclusive lock on the given blog ID.
     *
     * @param _id the blog ID to release a lock for
     */
    private void releaseLock( final BlogID _id ) {

        // get our lock (and we'd better darned well have one!)...
        Semaphore semaphore;
        synchronized( locks ) {
            semaphore = locks.get( _id.getID() );
            if( semaphore == null ) {
                LOG.error( "Trying to release a lock on a blog ID that has no lock: " + _id );
                return;
            }

            // release our permit and delete the lock if no other threads are waiting on it...
            semaphore.release();
            if( !semaphore.hasQueuedThreads() ) {
                locks.remove( _id.getID() );
            }
        }
    }
}
