package com.slightlyloony.blog.storage;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.MessageFormat;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogContentObjectCodec implements StorageCodec {

    private static final Logger LOG = LogManager.getLogger();


    /**
     * Uses the given file to create a blog object.  Note that the file isn't necessarily read by this method; the blog object produced may be
     * responsible for that.
     *
     * @param _file the file to use to create the blog object
     * @return the blog object created
     * @throws StorageException on any problem
     */
    @Override
    public BlogObject read( final File _file, final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements,
                            final ContentCompressionState _compressionState ) throws StorageException {

        try {
            StorageInputStream sis = new StorageInputStream( new FileInputStream( _file ), (int) _file.length() );
            StreamObjectContent soc = new StreamObjectContent( sis, _type.isCompressible() ? _compressionState : ContentCompressionState.DO_NOT_COMPRESS );
            return new BlogContentObject( _id, _type, _accessRequirements, soc );
        }
        catch( IOException e ) {
            String msg = MessageFormat.format( "Blog object file ({1}) problem: {0}", e.getMessage(), _file.getAbsolutePath() );
            LOG.error( msg, e );
            throw new StorageException( msg, e );
        }
    }


    /**
     * Creates a new file to persist the given blog object.
     *
     * @param _object the blog object to persist to a new file
     * @return the blog object persisted
     * @throws StorageException on any problem
     */
    @Override
    public BlogObject create( final BlogObject _object, final File _file ) throws StorageException {

        // write the contents out...
        BlogContentObject object = (BlogContentObject) _object;
        try(
            InputStream is = object.getStream();
            OutputStream os = new FileOutputStream( _file ); ) {
            ByteStreams.copy( is, os );
        }
        catch( IOException e ) {
            String msg = MessageFormat.format( "Blog object file problem: {0}", e.getMessage() );
            LOG.error( msg, e );
            throw new StorageException( msg, e );
        }

        try {
            // return our new object in a blog object...
            StorageInputStream sis = new StorageInputStream( new FileInputStream( _file ), (int) _file.length() );
            StreamObjectContent content = new StreamObjectContent( sis, object.getContent().getCompressionState() );
            return new BlogContentObject( object.getBlogID(), object.getType(), object.getAccessRequirements(), content );
        }
        catch( FileNotFoundException e ) {
            String msg = MessageFormat.format( "Blog object file ({1}) problem: {0}", e.getMessage(), _file.getAbsolutePath() );
            LOG.error( msg, e );
            throw new StorageException( msg, e );
        }
    }


    /**
     * Updates an existing representation of the given object to the given file (which must already exist).
     *
     * @param _object the blog object to persist to the given file
     * @param _file       the file to persist the given blog object into
     * @throws StorageException on any problem
     */
    @Override
    public BlogObject update( final BlogObject _object, final File _file ) throws StorageException {

        BlogContentObject object = (BlogContentObject) _object;

        try(
            // write the contents out...
            InputStream is = object.getStream();
            OutputStream os = new FileOutputStream( _file ); ) {
            ByteStreams.copy( is, os );
        }
        catch( IOException e ) {
            String msg = MessageFormat.format( "Blog object file ({0}) problem: {1}", _file.getAbsolutePath(), e.getMessage() );
            LOG.error( msg, e );
            throw new StorageException( msg, e );
        }

        try {
            // return our new object in a blog object...
            StorageInputStream sis = new StorageInputStream( new FileInputStream( _file ), (int) _file.length() );
            StreamObjectContent content = new StreamObjectContent( sis, object.getContent().getCompressionState() );
            return new BlogContentObject( _object.getBlogID(), _object.getType(), _object.getAccessRequirements(), content );
        }
        catch( FileNotFoundException e ) {
            String msg = MessageFormat.format( "Blog object file ({1}) problem: {0}", e.getMessage(), _file.getAbsolutePath() );
            LOG.error( msg, e );
            throw new StorageException( msg, e );
        }
    }
}
