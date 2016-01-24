package com.slightlyloony.blog.storage;

import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.util.S;
import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * Provides methods shared between codec implementations.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class StorageCodecBase {

    private static final Logger LOG = LogManager.getLogger();


    /**
     * Reads the given file (which is assumed to be encoded in UTF-8) and returns it as a string.  This is normally used for reading JSON.
     *
     * @param _file the file to read
     * @return the string read from the file
     * @throws StorageException on any problem
     */
    protected String getFileAsString( final File _file ) throws StorageException {

        try(
            InputStream is = new FileInputStream( _file ) ) {
            return S.fromUTF8( ByteStreams.toByteArray( is ) );
        }
        catch( IOException e ) {
            LOG.error( LU.msg( "Problem reading file {0}: {1}", _file.getName(), e.getMessage() ) );
            throw new StorageException( "Problem reading file: " + _file.getName() );
        }
    }


    /**
     * Writes the given string (UTF-8 encoded) to the given file.
     *
     * @param _string the string to write to the file
     * @param _file the file to write the string to
     * @throws StorageException on any problem
     */
    protected void writeStringToFile( final String _string, final File _file ) throws StorageException {

        try(
            OutputStream os = new FileOutputStream( _file ) ) {
            os.write( S.toUTF8( _string ) );
        }
        catch( IOException e ) {
            LOG.error( LU.msg( "Problem writing file {0}: {1}", _file.getName(), e.getMessage() ) );
            throw new StorageException( "Problem writing file: " + _file.getName() );
        }
    }
}
