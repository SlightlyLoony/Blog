package com.slightlyloony.blog.storage;

import com.google.common.collect.Maps;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.objects.BlogObject;
import com.slightlyloony.blog.objects.BlogObjectType;
import com.slightlyloony.blog.objects.ContentCompressionState;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Provides access to the storage system (disk) for the blog.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Storage {

    private static final Logger LOG = LogManager.getLogger();

    private final File objectsRoot;
    private final Map<String,Semaphore> locks;


    /**
     * Creates a new instance of this class that will store data in the directory specified by the given objectsRoot path.
     *
     * @param _rootPath The path to the content objectsRoot directory
     */
    public Storage( String _rootPath ) {

        if( _rootPath == null )
            throw new HandlerIllegalArgumentException( "Missing path information" );

        objectsRoot = new File( _rootPath, Constants.OBJECTS_ROOT );
        locks = Maps.newHashMap();

        // do a little sanity checking, to make sure that we actually HAVE this directory and that we can write into it...
        if( !objectsRoot.exists() || !objectsRoot.isDirectory() || !objectsRoot.canWrite() )
            throw new HandlerIllegalStateException( "Path supplied for content objectsRoot doesn''t exist, isn''t a directory, or isn''t writable: " + _rootPath );
    }


    /**
     * Reads the blog object with the given ID, type, and access requirements.  The ID and type are required.  The access requirements are optional;
     * if missing (null) then this is an internal request.  <i>All</i> requests for external access <i>must</i> include access requirements, as this
     * is used as part of the file name.  If an error occurs, an invalid blog object is returned.
     *
     * @param _id the blog object ID for the desired object
     * @param _type the blog object type for the desired object
     * @param _accessRequirements the optional access requirements (for external requests only)
     * @param _compressionState the compression state of this object
     * @return the blog object read
     * @throws StorageException on any problem
     */
    public BlogObject read( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements,
                            final ContentCompressionState _compressionState ) throws StorageException {

        if( (_id == null) || (_type == null) )
            throw new HandlerIllegalArgumentException( "Missing IntegerDatum or type" );

        // get a lock for this blog ID...
        getLock( _id );

        try {
            // get the file...
            File file = getObjectFile( _id, _type, _accessRequirements );

            // make sure it exists and we can read it...
            if( (!file.exists()) || (!file.isFile()) || (!file.canRead()) ) {
                String msg = MessageFormat.format( "Blog object file ({0}) problem: doesn''t exist, isn''t a file, or can''t read",
                        file.getAbsolutePath() );
                LOG.warn( msg );
                throw new StorageException( msg );
            }

            // create our blog object and return it...
            return _type.getCodec().read( file, _id, _type, _accessRequirements, _compressionState );
        }

        // now release our lock...
        finally {
            releaseLock( _id );
        }
    }


    // TODO: redo content length to be an object (and nullable) to deal with unknown lengths.  Review the use of content length through the stack
    // TODO: Review use of compression state as a parameter - is it REALLY the right way to do this?
    /**
     * Creates a new file to persist the given blog object.  This object's blog ID should have been created just before this method is invoked, to
     * minimize the possibility that the server could go down with the object not persisted.
     *
     * @param _object the object to be persisted
     * @return the blog object representing the shiny new object
     * @throws StorageException on any problem
     */
    public BlogObject create( final BlogObject _object ) throws StorageException {

        if( _object == null )
            throw new HandlerIllegalArgumentException( "Missing blog object to create" );

        // get a lock for this blog ID...
        getLock( _object.getBlogID() );

        // get the file...
        File file = getObjectFile( _object.getBlogID(), _object.getType(), _object.getAccessRequirements() );

        try {

            // create the file, or fail trying...
            if( !file.createNewFile() ) {
                String msg = MessageFormat.format( "Blog object file ({0}) problem: already exists, or we can''t create it",
                        file.getAbsolutePath() );
                LOG.warn( msg );
                throw new StorageException( msg );
            }

            return _object.getType().getCodec().create( _object, file );
        }

        catch( IOException e ) {
            String msg = MessageFormat.format( "Blog object file ({0}) problem: {1}", file.getAbsolutePath(), e.getMessage() );
            LOG.error( msg, e );
            throw new StorageException( msg, e );
        }

        // now release our lock...
        finally {
            releaseLock( _object.getBlogID() );
        }
    }


    /**
     * Updates an existing blog object with the new content given blog object.  The returned blog object contains a stream for the updated blog
     * object.
     *
     * @param _object the blog object with updated content
     * @return the blog object representing the shiny new object
     * @throws StorageException on any problem
     */
    public BlogObject update( final BlogObject _object ) throws StorageException {

        if( _object == null )
            throw new HandlerIllegalArgumentException( "Missing blog object to modify" );

        // get a lock...
        getLock( _object.getBlogID() );

        try {
            // get the file...
            File file = getObjectFile( _object.getBlogID(), _object.getType(), _object.getAccessRequirements() );

            // make sure it exists and we can write it...
            if( (!file.exists()) || (!file.isFile()) || (!file.canWrite()) ) {
                String msg = MessageFormat.format( "Blog object file ({0}) problem: doesn''t exist, isn''t a file, or can''t write", file.getAbsolutePath() );
                LOG.warn( msg );
                throw new StorageException( msg );
            }

            return _object.getType().getCodec().update( _object, file );
        }

        // now release our lock...
        finally {
            releaseLock( _object.getBlogID() );
        }
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
                LOG.error( "Trying to release a lock on a blog IntegerDatum that has no lock: " + _id );
                return;
            }

            // release our permit and delete the lock if no other threads are waiting on it...
            semaphore.release();
            if( !semaphore.hasQueuedThreads() ) {
                locks.remove( _id.getID() );
            }
        }
    }


    private File getObjectFile( final BlogID _id,
                                final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements  ) {

        String fileName = _id.getID() + ((_accessRequirements == null) ? "" : _accessRequirements.getCode() ) + "." + _type.getExtension();
        return new File( getObjectDir( _id ), fileName );
    }


    private File getObjectDir( final BlogID _id ) {

        // get the path to the containing directory...
        File path = objectsRoot;
        for( int i = 0; i < 4; i++ ) {
            int initialChar = i << 1;
            String element = _id.getID().substring( initialChar, initialChar + 2 );
            path = new File( path, element );
        }
        return path;
    }


    public File getObjects() {
        return objectsRoot;
    }
}
