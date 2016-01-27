package com.slightlyloony.blog.objects;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.storage.StorageInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encapsulates the notion of content to be written to an HTTP response.  The content may be available either as an array of bytes or as a stream,
 * and the data may be uncompressed and possibly compressible, not to be compressed (because it's already compressed, like a JPG, or is known to
 * be uncompressable), or compressed.  The {@link #write(BlogRequest,BlogResponse,boolean)} method writes the
 * instances content &ndash; no matter what form it's in &ndash; to the blog response.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BytesObjectContent extends BlogObjectContent {

    private static final Logger LOG = LogManager.getLogger();

    private byte[] content;


    public BytesObjectContent( final byte[] _content, final ContentCompressionState _compressionState, final int _contentLength ) {
        super( _compressionState, _contentLength );

        if( _content == null )
            throw new HandlerIllegalArgumentException( "Missing content" );

        if( _content.length > _contentLength )
            throw new HandlerIllegalArgumentException( "Actual content is longer than specified length: " + _content.length + " vs. " + _contentLength );

        content = _content;
    }


    @Override
    public void write(  final BlogRequest _request, final BlogResponse _response, final boolean _mayCompress  ) {

        if( compressionState.mayCompress() && _mayCompress )
            if( _request.acceptsGZIP() )
                copy( _response, contIS(), gzipOS( respOS( _response ) ), true, -1 );
            else
                copy( _response, contIS(), respOS( _response ), false, content.length );
        else
            if( compressionState.isCompressed() )
                if( _request.acceptsGZIP() )
                    copy( _response, contIS(), respOS( _response ), true, content.length );
                else
                    copy( _response, gzipIS( contIS() ), respOS( _response ), false, -1 );
            else
                copy( _response, contIS(), respOS( _response ), false, content.length );
    }


    @Override
    protected InputStream contIS() {
        return new ByteArrayInputStream( content );
    }


    @Override
    public int size() {
        return 8 + content.length;
    }


    @Override
    public BytesObjectContent asBytes() {
        return this;
    }


    @Override
    public BytesObjectContent asCompressedBytes( final boolean _mayCompress ) {

        // if we're not already compressed, but it's ok for us to do so, then do it...
        if( !compressionState.isCompressed() && compressionState.mayCompress() && _mayCompress ) {

            byte[] compressedBytes = compress( content );
            if( compressedBytes.length < content.length ) {
                content = compressedBytes;
                compressionState = ContentCompressionState.COMPRESSED;
            }
            else
                compressionState = ContentCompressionState.DO_NOT_COMPRESS;
        }
        return this;
    }


    private byte[] compress( final byte[] _uncompressed ) {

        try (
                // make our output buffer 20% larger than the input, just in case we inflate a bit...
                ByteArrayOutputStream baos = new ByteArrayOutputStream( _uncompressed.length * 5 / 4 );

                OutputStream os = new GZIPOutputStream( baos, true );
                InputStream is = new ByteArrayInputStream( _uncompressed ) ) {

            ByteStreams.copy( is, os );

            // we have to close to flush the compressor...
            is.close();
            os.close();

            return baos.toByteArray();
        }
        catch( IOException e ) {
            String msg = "Problem trying to compress bytes";
            LOG.error( msg, e );
            throw new HandlerIllegalStateException( msg, e );
        }
    }


    private byte[] decompress( final byte[] _compressed ) {

        try (
                // make our output buffer 300% larger than the input, to reduce possible growths...
                ByteArrayOutputStream baos = new ByteArrayOutputStream( _compressed.length * 3 );

                InputStream is = new GZIPInputStream( new ByteArrayInputStream( _compressed ) ) ) {

            ByteStreams.copy( is, baos );

            // we have to close to flush the compressor...
            is.close();
            baos.close();

            return baos.toByteArray();
        }
        catch( IOException e ) {
            String msg = "Problem trying to decompress bytes";
            LOG.error( msg, e );
            throw new HandlerIllegalStateException( msg, e );
        }
    }


    @Override
    public StreamObjectContent asStream() {

        StorageInputStream sis = new StorageInputStream( new ByteArrayInputStream( content ), contentLength );
        return new StreamObjectContent( sis, compressionState );
    }


    public byte[] getBytes() {
        return content;
    }
}
