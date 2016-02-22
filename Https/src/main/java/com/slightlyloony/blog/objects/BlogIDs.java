package com.slightlyloony.blog.objects;

import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.storage.Constants;
import com.slightlyloony.blog.util.ID;
import com.slightlyloony.blog.util.Timer;
import com.slightlyloony.common.ExecutionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * The main responsibility of this singleton class is to issue a new blog object ID when one is needed.  To do this, it examines the file system
 * upon startup to find the highest blog object ID that's actually on the file system, then issues new IDs starting at the next one.  Key to the
 * integrity of this scheme is that new blog object IDs should be issued as new objects are created and written.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogIDs {

    public static final BlogIDs INSTANCE = new BlogIDs();

    private static final Logger LOG = LogManager.getLogger();

    private long lastUsedID;


    /**
     * Reads the file system to determine the last used blog object ID.
     */
    public void init() {

        Timer t = new Timer();

        // get the root of our object storage...
        File dir = new File( ServerInit.getConfig().getContentRoot(), Constants.OBJECTS_ROOT );

        // dive down through four levels of highest used two-digit directory names...
        // TODO: note that this will fail if there are any FILES with valid base64url two character names in these directories...
        // TODO: the integrity check does look for this, but only every 30 minutes...
        for( int i = 0; i < 4; i++ ) {

            // sort dirs by base64url value of name...
            String[] dirs = dir.list( ( _dir, _name ) -> _name.length() == 2 && ID.isValid( _name ) );
            Arrays.sort( dirs, ( _name1, _name2 ) -> -1 * ID.compare( _name1, _name2 ) );

            // the first result is the highest used dir; concatenate it...
            dir = new File( dir, dirs[0] );
        }

        // now get the highest file name, using just the first 10 characters of each file name...
        String[] files = dir.list( (_dir, _name) -> (_name.length() >= 10) && ID.isValid( _name.substring( 0, 10 ) ) );
        Arrays.sort( files, ( _name1, _name2 ) -> -1 * ID.compare( _name1.substring( 0, 10 ), _name2.substring( 0, 10 ) ) );

        // get the value we found...
        // TODO: note that this will fail if the highest directory is empty...
        lastUsedID = ID.decode( files[0].substring( 0, 10 ) );

        t.mark();
        LOG.info( msg( "Found highest used blog object ID ({0}) in {1}", lastUsedID, t.toString() ) );

        // schedule an integrity check every 30 minutes...
        ExecutionService.INSTANCE.scheduleAtFixedRate( (Runnable) BlogIDs.INSTANCE::integrityCheck, 30, 30, TimeUnit.MINUTES );
    }


    /**
     * Reads the file system to see if there are any missing blog object IDs.
     *
     * @return true if the blog object IDs check out ok, with no errors
     */
    public boolean integrityCheck() {

        Timer t = new Timer();

        // get the root of our object storage and verify that it's valid...
        File dir = new File( ServerInit.getConfig().getContentRoot(), Constants.OBJECTS_ROOT );
        if( !dir.exists() || !dir.isDirectory() || !dir.canRead() || !dir.canWrite() ) {
            LOG.fatal( msg( "Blog object root {0} doesn't exist, isn't a directory, can't be read, or can't be written", dir.getAbsolutePath() ) );
            return false;
        }

        // recursively examine our directories and verify validity of their contents...
        boolean result = examine( dir, "", true );

        t.mark();
        LOG.info( msg( "Checked blog object store integrity in {0}", t.toString() ) );

        return result;
    }


    private boolean examine( final File _dir, final String _path, final boolean _truth ) {

        boolean newTruth = _truth;

        // get all our entries, sorted by name...
        File[] entries = _dir.listFiles();
        if( entries == null )
            throw new HandlerIllegalStateException( "Given entry is not a directory: " + _dir.getName() );
        Arrays.sort( entries, ( _file1, _file2 ) -> _file1.getName().compareTo( _file2.getName() ) );

        // if we're at the lowest level, look for files...
        if( _path.length() == 8 ) {

            // iterate over all our files, making sure they're files and in the right sequence...
            int expectedEntry = 0;
            for( File entry : entries ) {

                String name = entry.getName();

                // skip any hidden files...
                if( entry.isHidden() )
                    continue;

                // make sure we have an actual usable file here..
                if( !entry.exists() || !entry.isFile() || !entry.canRead() || !entry.canWrite() ) {
                    LOG.error( msg( "Entry {0} doesn't exist, isn't a file, can't be read, or can't be written", name ) );
                    return false;
                }

                // make sure our file name is at least 10 characters and is comprised of valid base64url characters...
                if( (name.length() < 10) || !ID.isValid( name.substring( 0, 10 ) ) ) {
                    LOG.error( msg( "Entry {0} isn't named with a valid blog ID", name ) );
                    return false;
                }

                // make sure the first 8 digits match our directory hierarchy...
                if( !_path.equals( name.substring( 0, 8 ) ) ) {
                    LOG.error( msg( "Entry {0} isn't in the right directory: {1}", name, _dir.getAbsolutePath() ) );
                    return false;
                }

                // make sure the last 2 digits are what we expected...
                String lastTwo = name.substring( 8, 10 );
                int val = (ID.get( lastTwo.charAt( 0 ) ) << 6) + ID.get( lastTwo.charAt( 1 ) );
                if( val != expectedEntry ) {
                    String expectedDigits = "" + ID.get( expectedEntry >>> 6 ) + ID.get( expectedEntry & 0x3f );
                    LOG.error( msg( "Entry {0} doesn't have the expected last two digits: {1}", name, expectedDigits ) );
                    return false;
                }

                expectedEntry++;
            }

            return newTruth;
        }

        // otherwise, look for directories...
        else {

            // iterate over all our files, making sure they're files and in the right sequence...
            int expectedEntry = 0;
            for( File entry : entries ) {

                String name = entry.getName();

                // skip any hidden files...
                if( entry.isHidden() )
                    continue;

                // make sure we have an actual usable directory here..
                if( !entry.exists() || !entry.isDirectory() || !entry.canRead() || !entry.canWrite() ) {
                    LOG.error( msg( "Entry {0} doesn't exist, isn't a directory, can't be read, or can't be written", name ) );
                    return false;
                }

                // make sure our file name is exactly 2 characters and is comprised of valid base64url characters...
                if( (name.length() != 2) || !ID.isValid( name.charAt( 0 ) ) || !ID.isValid( name.charAt( 1 ) ) ) {
                    LOG.error( msg( "Entry {0} isn't named with a valid blog ID 2 digit part", name ) );
                    return false;
                }

                // make sure the 2 digits are what we expected...
                int val = (ID.get( name.charAt( 0 ) ) << 6) + ID.get( name.charAt( 1 ) );
                if( val != expectedEntry ) {
                    String expectedDigits = "" + ID.get( expectedEntry >>> 6 ) + ID.get( expectedEntry & 0x3f );
                    LOG.error( msg( "Entry {0} doesn't have the expected name: {1}", name, expectedDigits ) );
                    return false;
                }

                expectedEntry++;

                // now go explore the subdirectory...
                newTruth = newTruth && examine( new File( _dir, name ), _path + name, newTruth );
            }

            return newTruth;
        }
    }


    /**
     * Returns the next available (unused) blog object ID, in sequential order.
     *
     * @return the next available (unused) blog object ID
     */
    public synchronized BlogID getNextBlogID() {

        BlogID result =  BlogID.create( ID.encode( ++lastUsedID ) );
        if( result == null)
            throw new HandlerIllegalStateException( "Could not create next blog object ID" );

        return result;
    }


    private BlogIDs() {
        // prevent instantiation...
    }
}
