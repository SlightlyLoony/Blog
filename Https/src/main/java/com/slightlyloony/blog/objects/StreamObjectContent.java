package com.slightlyloony.blog.objects;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.BlogRequest;
import com.slightlyloony.blog.handlers.BlogResponse;
import com.slightlyloony.blog.storage.StorageInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StreamObjectContent extends BlogObjectContent {

    private static final Logger LOG = LogManager.getLogger();

    private static final int ASSUMED_MEMORY_SIZE = 8192;  // this mainly depends on the buffer size, which we don't know, so we'll just guess...

    private final StorageInputStream content;


    public StreamObjectContent( final StorageInputStream _content, final ContentCompressionState _compressionState ) {
        super( _compressionState, (_content == null) ? 0 : _content.length() );

        if( _content == null )
            throw new IllegalArgumentException( "Missing content" );

        content = _content;
    }


    @Override
    public void write( final BlogRequest _request, final BlogResponse _response, final boolean _mayCompress ) {

        if( compressionState.mayCompress() && _mayCompress )
            if( _request.acceptsGZIP() )
                copy( _response, contIS(), gzipOS( respOS( _response ) ), true, -1 );
            else
                copy( _response, contIS(), respOS( _response ), false, content.length() );
        else
            if( compressionState.isCompressed() )
                if( _request.acceptsGZIP() )
                    copy( _response, contIS(), respOS( _response ), true, content.length() );
                else
                    copy( _response, gzipIS( contIS() ), respOS( _response ), false, -1 );
            else
                copy( _response, contIS(), respOS( _response ), false, content.length() );
    }


    private void copy( final OutputStream _outputStream ) {

        try {
            ByteStreams.copy( content, _outputStream );
        }
        catch( IOException e ) {
            String msg = "Problem copying content input stream to response output stream";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }
    }


    private void copyDecompressed( final OutputStream _outputStream ) {

        try(
            InputStream inputStream = new GZIPInputStream( content ) ) {
            ByteStreams.copy( inputStream, _outputStream );
        }
        catch( IOException e ) {
            String msg = "Problem copying GZIP decompressing input stream to response output stream";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }
    }


    @Override
    public BytesObjectContent asBytes() {

        byte[] bytes = new byte[contentLength];
        try( StorageInputStream sis = content ) {
            int i = 0;
            while( i < bytes.length ) {
                int read = sis.read( bytes, i, bytes.length - i );
                if( read < 1 )
                    break;
                i += read;
            }
        }
        catch( IOException e ) {
            String msg = "Error converting storage input stream to bytes";
            LOG.error( msg, e );
            throw new IllegalStateException( msg, e );
        }

        // return it as a bytes content instance...
        return new BytesObjectContent( bytes, compressionState, contentLength );
    }


    @Override
    protected InputStream contIS() {
        return content;
    }


    @Override
    public BytesObjectContent asCompressedBytes( final boolean _mayCompress ) {
        return asBytes().asCompressedBytes( _mayCompress );
    }


    @Override
    public StreamObjectContent asStream() {
        return this;
    }


    @Override
    public int memorySize() {
        return ASSUMED_MEMORY_SIZE;
    }


    public StorageInputStream getStream() {
        return (StorageInputStream) content;
    }
}
