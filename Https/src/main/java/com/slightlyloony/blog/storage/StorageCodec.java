package com.slightlyloony.blog.storage;

import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;

import java.io.File;

/**
 * Implemented by classes that can read blog objects from a file and write blog objects to a file.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface StorageCodec {


    /**
     * Uses the given file to create a blog object.  Note that the file isn't necessarily read by this method; the blog object produced may be
     * responsible for that.
     *
     * @param _file the file to use to create the blog object
     * @param _id the blog ID for the blog object being read
     * @param _type the blog object type
     * @param _accessRequirements the blog object's access requirements
     * @param _compressionState the blog object's compression state
     * @return the blog object created
     * @throws StorageException on any problem
     */
    BlogObject read( final File _file, final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements,
                     final ContentCompressionState _compressionState ) throws StorageException;


    /**
     * Creates a new file to persist the given blog object.
     *
     * @param _object the blog object to persist to a new file
     * @param _file  the file to persist the given blog object into
     * @return the blog object persisted
     * @throws StorageException on any problem
     */
    BlogObject create( final BlogObject _object, final File _file ) throws StorageException;


    /**
     * Updates an existing representation of the given object to the given file (which must already exist).
     *
     * @param _object the blog object to persist to the given file
     * @param _file the file to persist the given blog object into
     * @throws StorageException on any problem
     */
    BlogObject update( final BlogObject _object, final File _file ) throws StorageException;
}
