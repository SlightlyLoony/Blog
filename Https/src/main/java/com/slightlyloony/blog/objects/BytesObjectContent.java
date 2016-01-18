package com.slightlyloony.blog.objects;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.storage.StorageInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * Encapsulates the notion of content to be written to an HTTP response.  The content may be available either as an array of bytes or as a stream,
 * and the data may be uncompressed and possibly compressible, not to be compressed (because it's already compressed, like a JPG, or is known to
 * be uncompressable), or compressed.  The {@link #write(com.slightlyloony.blog.handlers.BlogResponse,boolean,boolean,boolean)} method writes the
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
            throw new IllegalArgumentException( "Missing content" );

        if( _content.length > _contentLength )
            throw new IllegalArgumentException( "Actual content is longer than specified length: " + _content.length + " vs. " + _contentLength );

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
                copy( _response, contIS(), respOS( _response ), true, content.length );
    }


    @Override
    protected InputStream contIS() {
        return new ByteArrayInputStream( content );
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


    @Override
    public StreamObjectContent asStream() {

        StorageInputStream sis = new StorageInputStream( new ByteArrayInputStream( content ), contentLength );
        return new StreamObjectContent( sis, compressionState, contentLength );
    }


    public synchronized String getUTF8String() {

        // get the bytes, convert them to a string, and return that...
        return new String( content, Charset.forName( "UTF-8" ) );
    }


    @Override
    public int memorySize() {
        return ((byte[])content).length;
    }


    public byte[] getBytes() {
        return (byte[]) content;
    }
}
