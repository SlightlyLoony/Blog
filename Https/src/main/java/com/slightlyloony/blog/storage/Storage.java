package com.slightlyloony.blog.storage;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.MessageFormat;

/**
 * Provides access to the storage system (disk) for the blog.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Storage {

    private static final Logger LOG = LogManager.getLogger();

    private final File objectsRoot;


    /**
     * Creates a new instance of this class that will store data in the directory specified by the given objectsRoot path.
     *
     * @param _rootPath The path to the content objectsRoot directory
     */
    public Storage( String _rootPath ) {

        if( _rootPath == null )
            throw new IllegalArgumentException( "Missing path information" );

        objectsRoot = new File( _rootPath, Constants.OBJECTS_ROOT );

        // do a little sanity checking, to make sure that we actually HAVE this directory and that we can write into it...
        if( !objectsRoot.exists() || !objectsRoot.isDirectory() || !objectsRoot.canWrite() )
            throw new IllegalStateException( "Path supplied for content objectsRoot doesn't exist, isn't a directory, or isn't writable: " + _rootPath );
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
     */
    public BlogObject read( final BlogID _id, final BlogObjectType _type,
                            final BlogObjectAccessRequirements _accessRequirements, final ContentCompressionState _compressionState ) {

        if( (_id == null) || (_type == null) )
            throw new IllegalArgumentException( "Missing ID or type" );

        // get the file...
        File file = getObjectFile( _id, _type, _accessRequirements );

        // make sure it exists and we can read it...
        if( (!file.exists()) || (!file.isFile()) || (!file.canRead()) ) {
            String msg = MessageFormat.format( "Blog object file (\"{0}\") problem: doesn't exist, isn't a file, or can't read", file.getAbsolutePath() );
            LOG.warn( msg );
            return new BlogObject( _id, _type, _accessRequirements );
        }

        // create our blog object and return it...
        try {
            StorageInputStream sis = new StorageInputStream( new FileInputStream( file ), (int) file.length() );
            StreamObjectContent soc = new StreamObjectContent( sis, _compressionState );
            return new BlogObject( _id, _type, _accessRequirements, soc );
        }
        catch( FileNotFoundException e ) {
            String msg = MessageFormat.format( "Blog object file (\"{1}\") problem: {0}", e.getMessage(), file.getAbsolutePath() );
            LOG.error( msg, e );
            return new BlogObject( _id, _type, _accessRequirements );
        }
    }


    /**
     * Creates a new blog object with the given content, type, and access requirements.  The access requirements are optional; if missing (null) then
     * this will be an object accessible only through an internal request.  The returned blog object contains a stream for the newly created blog
     * object, which contains its newly assigned blog object ID.
     *
     * @param _content the content of the new object
     * @param _type the blog object type of the new object
     * @param _accessRequirements the optional access requirements (for externally available objects only) for the new object
     * @param _compressionState the compression state of this object
     * @return the blog object representing the shiny new object
     */
    public BlogObject create( final BlogObjectContent _content, final BlogObjectType _type,
                              final BlogObjectAccessRequirements _accessRequirements, final ContentCompressionState _compressionState ) {

        if( (_content == null) || (_type == null) )
            throw new IllegalArgumentException( "Missing content or type" );

        // get us a blog ID...
        BlogID newID = BlogIDs.INSTANCE.getNextBlogID();

        // get the file...
        File file = getObjectFile( newID, _type, _accessRequirements );

        try {
            // create the file, or fail trying...
            if( !file.createNewFile() ) {
                String msg = MessageFormat.format( "Blog object file (\"{0}\") problem: already exists, or we can't create it", file.getAbsolutePath() );
                LOG.warn( msg );
                return new BlogObject( newID, _type, _accessRequirements );
            }

            // write the contents out...
            try(
                InputStream is = _content.asStream().getStream();
                OutputStream os = new FileOutputStream( file ); ) {
                ByteStreams.copy( is, os );
            }
        }
        catch( IOException e ) {
            String msg = MessageFormat.format( "Blog object file (\"{0}\") problem: {1}", file.getAbsolutePath(), e.getMessage() );
            LOG.error( msg, e );
            return new BlogObject( newID, _type, _accessRequirements );
        }

        try {
            // return our new object in a blog object...
            StorageInputStream sis = new StorageInputStream( new FileInputStream( file ), (int) file.length() );
            StreamObjectContent content = new StreamObjectContent( sis, _compressionState );
            return new BlogObject( newID, _type, _accessRequirements, content );
        }
        catch( FileNotFoundException e ) {
            String msg = MessageFormat.format( "Blog object file (\"{1}\") problem: {0}", e.getMessage(), file.getAbsolutePath() );
            LOG.error( msg, e );
            return new BlogObject( newID, _type, _accessRequirements );
        }
    }


    /**
     * Updates an existing blog object with the new content given blog object.  The returned blog object contains a stream for the updated blog
     * object.
     *
     * @param _object the blog object with updated content
     * @return the blog object representing the shiny new object
     */
    public BlogObject modify( final BlogObject _object ) {

        if( _object == null )
            throw new IllegalArgumentException( "Missing blog object to modify" );

        // get the file...
        File file = getObjectFile( _object.getBlogID(), _object.getType(), _object.getAccessRequirements() );

        // make sure it exists and we can read it...
        if( (!file.exists()) || (!file.isFile()) || (!file.canRead()) ) {
            String msg = MessageFormat.format( "Blog object file (\"{0}\") problem: doesn't exist, isn't a file, or can't read", file.getAbsolutePath() );
            LOG.warn( msg );
            return new BlogObject( _object.getBlogID(), _object.getType(), _object.getAccessRequirements() );
        }

        try {

            // write the contents out...
            try(
                InputStream is = _object.getContent().asStream().getStream();
                OutputStream os = new FileOutputStream( file ); ) {
                ByteStreams.copy( is, os );
            }
        }
        catch( IOException e ) {
            String msg = MessageFormat.format( "Blog object file (\"{0}\") problem: {1}", file.getAbsolutePath(), e.getMessage() );
            LOG.error( msg, e );
            return new BlogObject( _object.getBlogID(), _object.getType(), _object.getAccessRequirements() );
        }

        try {
            // return our new object in a blog object...
            StorageInputStream sis = new StorageInputStream( new FileInputStream( file ), (int) file.length() );
            StreamObjectContent content = new StreamObjectContent( sis, _object.getContent().getCompressionState() );
            return new BlogObject( _object.getBlogID(), _object.getType(), _object.getAccessRequirements(), content );
        }
        catch( FileNotFoundException e ) {
            String msg = MessageFormat.format( "Blog object file (\"{1}\") problem: {0}", e.getMessage(), file.getAbsolutePath() );
            LOG.error( msg, e );
            return new BlogObject( _object.getBlogID(), _object.getType(), _object.getAccessRequirements() );
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
