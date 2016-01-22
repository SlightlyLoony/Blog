package com.slightlyloony.blog.storage;

import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.util.ID;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over all the blog objects stored on the system, returning an object containing information about each of the blog objects.  The iterator
 * makes no attempt to validate the entries on the system; it simply iterates over them in ID order until there are no more objects.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObjectIterator implements Iterator<BlogObjectIterator.BlogObjectInfo> {

    private File[][] level;
    private int[] index;
    private BlogObjectInfo info;

    public BlogObjectIterator() {

        level = new File[5][];
        index = new int[5];
        info = null;

        // initialize everything to the beginning...
        // get the root of our object storage...
        File dir = new File( ServerInit.getConfig().getContentRoot(), Constants.OBJECTS_ROOT );

        // dive down through four levels of highest used two-digit directory names...
        for( int i = 0; i < 4; i++ ) {

            // get our directories and stuff them away...
            level[i] = getDirs( dir );

            // follow the first one down the rabbit hole...
            dir = level[i][0];
        }

        // now get the files in the lowest directory...
        level[4] = getFiles( dir );
    }


    /**
     * Returns {@code true} if the iteration has more elements. (In other words, returns {@code true} if {@link #next} would return an element rather
     * than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {

        if( info != null )
            return true;

        // find the lowest directory with remaining elements to explore...
        int i = 4;
        for( ; (i >= 0)  && (index[i] >= level[i].length); i-- )
            if( i > 0 ) index[i-1]++;

        // if there's anything left to do...
        if( i >= 0 ) {

            // make sure all our directories are expanded...
            for( ; i < 3; i++ ) {
                level[i+1] = getDirs( level[i][index[i]] );
                index[i+1] = 0;
            }

            // if we need to, expand the lower directory into files for level 4...
            if( i < 4 ) {
                level[4] = getFiles( level[3][index[3]] );
                index[4] = 0;
            }

            // we must have something left to do - so fill in our info object and we're done...
            info = new BlogObjectInfo();
            info.file = level[4][index[4]];
            info.id = BlogID.create( info.file.getName().substring( 0, 10 ) );
            index[4]++;
        }
        else
            info = null;

        return (info != null);
    }


    private File[] getFiles( File _parent ) {

        // now get the files in the lowest directory...
        File[] files = _parent.listFiles( ( _file, _name ) -> {
            File file = new File( _file, _name );
            return file.exists() && file.isFile() && !file.isHidden() && (_name.length() >= 10 && ID.isValid( _name.substring( 0, 10 ) )); } );

        // sort it in base64url encoded order...
        Arrays.sort( files, ( _file1, _file2 ) -> ID.compare( _file1.getName().substring( 0, 10 ), _file2.getName().substring( 0, 10 ) ) );

        return files;
    }


    private File[] getDirs( File _parent ) {

        // get everything that looks like a two character directory...
        File[] dirs = _parent.listFiles( ( _dir, _name ) -> {
            File file = new File( _dir, _name );
            return file.exists() && file.isDirectory() && !file.isHidden() && (_name.length() == 2 && ID.isValid( _name ));  } );

        // sort it in base64url encoded order...
        Arrays.sort( dirs, ( _file1, _file2 ) -> ID.compare( _file1.getName(), _file2.getName() ) );

        return dirs;
    }


    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public BlogObjectInfo next() {

        if( (info == null) && !hasNext() )
            throw new NoSuchElementException( "No more blog objects" );

        BlogObjectInfo result = info;
        info = null;
        return result;
    }


    public static class BlogObjectInfo {

        public BlogID id;
        public File file;
    }
}
