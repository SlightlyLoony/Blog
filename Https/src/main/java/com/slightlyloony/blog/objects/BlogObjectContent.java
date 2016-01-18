package com.slightlyloony.blog.objects;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encapsulates the notion of content to be written to an HTTP response.  The content may be available either as an array of bytes or as a stream,
 * and the data may be uncompressed and possibly compressible, not to be compressed (because it's already compressed, like a JPG, or is known to
 * be uncompressable), or compressed.  The {@link #write(com.slightlyloony.blog.handlers.BlogResponse,boolean,boolean,boolean)} method writes the
 * instances content &ndash; no matter what form it's in &ndash; to the blog response.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class BlogObjectContent {

    private static final Logger LOG = LogManager.getLogger();
    private final static byte[] CRLF = new byte[] { (byte) 13, (byte) 10 };
    private final static byte[] TERMINAL_CHUNK = new byte[] { (byte) 13, (byte) 10, (byte) 48, (byte) 13, (byte) 10 };

    protected ContentCompressionState compressionState;
    protected final int contentLength;


    protected BlogObjectContent( final ContentCompressionState _compressionState, final int _contentLength ) {

        if( _compressionState == null )
            throw new IllegalArgumentException( "Missing compression state" );

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
        if( _setGZIP ) _response.setContentEncoding( "gzip" );

        // if we know the length, set the Content-Length...
        if( _length >= 0 ) _response.setContentLength( _length );

        // now copy the data...
        try {
            ByteStreams.copy( _inputStream, _outputStream );
        }
        catch( IOException e ) {
            String msg = "Problem copying content input stream to response output stream";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }
    }


    protected OutputStream gzipOS( final OutputStream _outputStream ) {

        try {
            return new GZIPOutputStream( _outputStream );
        }
        catch( IOException e ) {
            String msg = "Problem getting GZIP output stream";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }
    }


    protected InputStream gzipIS( final InputStream _inputStream ) {

        try {
            return new GZIPInputStream( _inputStream );
        }
        catch( IOException e ) {
            String msg = "Problem getting GZIP input stream";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }
    }


    protected OutputStream respOS( final BlogResponse _response ) {

        try {
            return _response.getOutputStream();
        }
        catch( IOException e ) {
            String msg = "Problem getting response output stream";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }
    }


    protected abstract InputStream contIS();


    protected byte[] compress( final byte[] _uncompressed ) {

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
            throw new IllegalStateException( msg, e );
        }
    }


    protected byte[] decompress( final byte[] _compressed ) {

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
            throw new IllegalStateException( msg, e );
        }
    }


    protected void setGZIP( final BlogResponse _response ) {
        _response.setContentEncoding( "gzip" );
    }


    public abstract BytesObjectContent asBytes();


    public abstract BytesObjectContent asCompressedBytes( final boolean _mayCompress );


    public abstract StreamObjectContent asStream();


    public ContentCompressionState getCompressionState() {
        return compressionState;
    }


    abstract public int memorySize();


    public int contentLength() {
        return contentLength;
    }
}
