package com.slightlyloony.blog.storage;

import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.MessageFormat;

/**
 * Provides access to the storage system (disk) for the blog.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Storage {

    private static final Logger LOG = LogManager.getLogger();

    private static final String OBJECTS_ROOT = "objects";

    private final File objectsRoot;


    /**
     * Creates a new instance of this class that will store data in the directory specified by the given objectsRoot path.
     *
     * @param _rootPath The path to the content objectsRoot directory
     */
    public Storage( String _rootPath ) {

        if( _rootPath == null )
            throw new IllegalArgumentException( "Missing path information" );

        objectsRoot = new File( _rootPath, OBJECTS_ROOT );

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
     * @return the blog object read
     */
    public BlogObject read( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements ) {

        if( (_id == null) || (_type == null) )
            throw new IllegalArgumentException( "Missing ID or type" );

        // get the path to the containing directory...
        File path = objectsRoot;
        for( int i = 0; i < 4; i++ ) {
            int initialChar = i << 1;
            String element = _id.getID().substring( initialChar, initialChar + 2 );
            path = new File( path, element );
        }

        // get the file...
        String fileName = _id.getID() + ((_accessRequirements == null) ? "" : _accessRequirements.getCode() ) + "." + _type.getExtension();
        File file = new File( path, fileName );

        // make sure it exists and we can read it...
        if( (!file.exists()) || (!file.isFile()) || (!file.canRead()) ) {
            String msg = MessageFormat.format( "Blog object file (\"{0}\") problem: doesn't exist, isn't a file, or can't read", file.getAbsolutePath() );
            LOG.warn( msg );
            return new BlogObject( _id, _type );
        }

        // create our blog object and return it...
        try {
            StorageInputStream sis = new StorageInputStream( new FileInputStream( file ), (int) file.length() );
            StreamObjectContent soc = new StreamObjectContent( sis, ContentCompressionState.UNCOMPRESSED, (int) file.length() );
            return new BlogObject( _id, _type, soc );
        }
        catch( FileNotFoundException e ) {
            String msg = MessageFormat.format( "Blog object file (\"{1}\") problem: {0}", e.getMessage(), file.getAbsolutePath() );
            LOG.error( msg, e );
            return new BlogObject( _id, _type );
        }
    }


    public File getObjectsRoot() {
        return objectsRoot;
    }
}
