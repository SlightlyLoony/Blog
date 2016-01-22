package com.slightlyloony.blog.storage;

import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.config.ServerConfig;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.handlers.HandlerIllegalStateException;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Implements a blog object cache on top of the storage system.  Entries are added to the cache as they are read, except that entries over a given
 * size are not cached (this is to avoid caching things like giant images, for example).  Entries are deleted from the cache when they are explicitly
 * invalidated, or when a new entry can't be added because the maximum size has been exceeded.  In the latter case, least recently used entries are
 * removed until the cache has enough space for the new entry.
 * <p>
 * Note that several independent caches are used instead of a single cache, with a separate cache for broad categories of item types.  For esample,
 * there is a cache just for metadata objects (which are small and very frequently accessed) and for images (which are generally much larger, and
 * less frequently accessed).  Having several caches also allows multiple threads to access the overall cache, though no more than one thread at
 * a time can access a single category cache.
 * <p>
 * This class can handle concurrent access by multiple threads; it is internally synchronized.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CachedStorage {

    private static final Logger LOG = LogManager.getLogger();

    private final Storage storage;
    private final int maxEntrySize;
    private final BlogObjectCache[] caches;



    public CachedStorage( final Storage _storage ) {

        storage = _storage;
        ServerConfig config = ServerInit.getConfig();
        maxEntrySize = config.getMaxCacheEntrySize();
        Map<String,ServerConfig.Cache> cacheConfigs = config.getCaches();

        // build our caches according to what we've configured...
        caches = new BlogObjectCache[cacheConfigs.size()];
        for( Map.Entry<String,ServerConfig.Cache> cacheEntry : cacheConfigs.entrySet() ) {
            BlogObjectUseCache use = BlogObjectUseCache.valueOf( cacheEntry.getKey() );
            caches[use.getOrdinal()] = new BlogObjectCache( use,
                    cacheEntry.getValue().getMaxCacheSize(),
                    cacheEntry.getValue().getAvgEntrySize() );
        }
    }


    /**
     * Reads the blog object with the given ID, type, and access requirements from the cache if available, otherwise from storage.  The ID and type
     * are required.  The access requirements are optional; if missing (null) then this is an internal request.  <i>All</i> requests for external
     * access <i>must</i> include access requirements, as this is used as part of the file name.  If an error occurs, an invalid blog object is
     * returned.
     *
     * @param _id the blog object ID for the desired object
     * @param _type the blog object type for the desired object
     * @param _accessRequirements the optional access requirements (for external requests only)
     * @return the blog object read
     */
    public BlogObject read( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements,
                            final ContentCompressionState _compressionState ) {

        // if we have a cache for this category of object, see if the object is cached...
        int cacheNum = _type.getCache().getOrdinal();
        if( (cacheNum >= 0) && (cacheNum < caches.length) && (caches[cacheNum] != null) ) {

            BlogObjectCache cache = caches[cacheNum];
            BlogObject cachedObj = cache.get( _id, _type );

            // if it was cached, we're done...
            if( cachedObj != null )
                return cachedObj;

            // it wasn't cached, so first we'll have to read it from storage...
            BlogObject readObj = storage.read( _id, _type, _accessRequirements, _compressionState );

            // if we got a valid object...
            if( readObj.isValid() ) {

                // if the content size is less than our threshold, we'll try caching it...
                BlogObjectContent content = readObj.getContent();
                if( content.contentLength() < maxEntrySize ) {

                    // make the blog object cacheable (resolve to bytes and try compressing)...
                    readObj.makeReadyForCache( _type.isCompressible() &&_compressionState.mayCompress() );

                    // tell the cache to take it...
                    cache.add( readObj );
                }
            }
            else {
                String msg = "Problem reading blog object " + _id + "." + _type;
                LOG.error( msg );
                throw new HandlerIllegalStateException( msg );
            }

            // leave with our shiny new object...
            return readObj;
        }

        // if we have no cache for this category, then we'll just have to read it from storage...
        return storage.read( _id, _type, _accessRequirements, _compressionState );
    }


    /**
     * Creates a new blog object with the given content, type, and access requirements, without affecting the cache at all.
     *
     * @param _content the content of the new object
     * @param _type the blog object type of the new object
     * @param _accessRequirements the optional access requirements (for externally available objects only) for the new object
     * @param _compressionState the compression state of this object
     * @return the blog object representing the shiny new object
     */
    public BlogObject create( final BlogObjectContent _content, final BlogObjectType _type,
                              final BlogObjectAccessRequirements _accessRequirements, final ContentCompressionState _compressionState ) {

        return storage.create( _content, _type, _accessRequirements, _compressionState );
    }


    /**
     * Updates an existing blog object with the new content given blog object.  The returned blog object contains a stream for the updated blog
     * object.  If the updated object was in the cache prior to the invocation of this method, it will be deleted and the new object will be cached
     * instead (if possible).
     *
     * @param _object the blog object with updated content
     * @return the blog object representing the shiny new object
     */
    public BlogObject modify( final BlogObject _object ) {

        if( _object == null )
            throw new HandlerIllegalArgumentException( "Missing blog object to modify" );

        // remember the ID and type...
        BlogID id = _object.getBlogID();
        BlogObjectType type = _object.getType();

        // do the modify operation...
        BlogObject object = storage.modify( _object );

        // if we have a valid object, and a cache for this kind of object...
        int cacheNum = _object.getType().getCache().getOrdinal();
        if( object.isValid() && (cacheNum >= 0) && (cacheNum < caches.length) && (caches[cacheNum] != null) ) {

            // get as much ready outside the synchronization block as we can...
            BlogObjectCache cache = caches[cacheNum];
            object.makeReadyForCache( type.isCompressible() );

            // synchronize around the invalidate/add pair, so we don't have another thread also trying to fill the cache for this guy...
            synchronized( cache.lock ) {

                // invalidate any existing entry...
                cache.remove( id, type );

                // then add a new entry, if we can...
                cache.add( object );
            }
         }

        return object;
    }


    public Storage getStorage() {
        return storage;
    }
}
