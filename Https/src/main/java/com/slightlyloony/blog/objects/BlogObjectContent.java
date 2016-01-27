package com.slightlyloony.blog.objects;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
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
public abstract class BlogObjectContent {

    private static final Logger LOG = LogManager.getLogger();

    protected ContentCompressionState compressionState;
    protected final Integer contentLength;


    /**
     * Creates a new instance of this class with the given compression state and content length (which may be null if unknown).
     *
     * @param _compressionState the current compression state of this content
     * @param _contentLength the current length of this content, or null if the length is unknown
     */
    protected BlogObjectContent( final ContentCompressionState _compressionState, final Integer _contentLength ) {

        if( _compressionState == null )
            throw new HandlerIllegalArgumentException( "Missing compression state" );

        compressionState = _compressionState;
        contentLength = _contentLength;
    }


    public abstract void write( final BlogRequest _request, final BlogResponse _response, final boolean _mayCompress );


    /**
     * Does the actual work of copying data to the response, from the given input stream to the given output stream.  Sets "gzip" Content-Encoding
     * if setGZIP is true, and Content-Length if the given length is zero or greater.  If the given length is negative, it's treated as unknown and
     * a chunked transfer is done (Jetty handles this automagically).
     *
     * @param _inputStream an input stream that provides the content to be transferred to the response
     * @param _outputStream the output stream that transfers content to the response
     * @param _setGZIP true if the content is gzip encoded
     * @param _length the length of the data being transferred, or negative if that length is unknown
     */
    protected void copy( final BlogResponse _response, final InputStream _inputStream, final OutputStream _outputStream,
                         final boolean _setGZIP, final int _length ) {

        // set gzip encoding if needed...
        if( _setGZIP )
            _response.setContentEncoding( "gzip" );

        // if we know the length, set the Content-Length...
        if( _length >= 0 ) _response.setContentLength( _length );

        // now copy the data...
        try {
            ByteStreams.copy( _inputStream, _outputStream );
        }
        catch( IOException e ) {
            String msg = "Problem copying content input stream to response output stream";
            LOG.error( msg );
            throw new HandlerIllegalStateException( msg, e );
        }
    }


    protected OutputStream gzipOS( final OutputStream _outputStream ) {

        try {
            return new GZIPOutputStream( _outputStream );
        }
        catch( IOException e ) {
            String msg = "Problem getting GZIP output stream";
            LOG.error( msg, e );
            throw new HandlerIllegalStateException( msg, e );
        }
    }


    protected InputStream gzipIS( final InputStream _inputStream ) {

        try {
            return new GZIPInputStream( _inputStream );
        }
        catch( IOException e ) {
            String msg = "Problem getting GZIP input stream";
            LOG.error( msg, e );
            throw new HandlerIllegalStateException( msg, e );
        }
    }


    protected OutputStream respOS( final BlogResponse _response ) {

        try {
            return _response.getOutputStream();
        }
        catch( IOException e ) {
            String msg = "Problem getting response output stream";
            LOG.error( msg, e );
            throw new HandlerIllegalStateException( msg, e );
        }
    }


    protected abstract InputStream contIS();


    public abstract int size();


    public abstract BytesObjectContent asBytes();


    public abstract BytesObjectContent asCompressedBytes( final boolean _mayCompress );


    public abstract StreamObjectContent asStream();


    public ContentCompressionState getCompressionState() {
        return compressionState;
    }


    public Integer contentLength() {
        return contentLength;
    }
}
