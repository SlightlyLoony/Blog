package com.slightlyloony.blog.storage;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a memory cache for blog objects.  The cache is a simple LRU cache with hashed access via the blog object ID.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObjectCache {

    public final Object lock = new Object();

    private static final Logger LOG = LogManager.getLogger();

    private final static boolean ACCESS_ORDER = true;
    private final static float LOAD_FACTOR = 0.7f;

    private final LinkedHashMap<BlogID,BlogObject> cache;
    private final long maxSize;
    private final BlogObjectUseCache useCache;

    private long currentSize;


    public BlogObjectCache( final BlogObjectUseCache _useCache, final long _maxSize, final long _estAvgSize ) {

        maxSize     = _maxSize;
        useCache    = _useCache;
        currentSize = 0;

        long estNumEntries = maxSize / _estAvgSize;
        int estCacheSlots = (int) Math.ceil( estNumEntries / LOAD_FACTOR );
        cache = new LinkedHashMap<>( estCacheSlots, LOAD_FACTOR, ACCESS_ORDER );
    }


    public BlogObject get( final BlogID _id, final BlogObjectType _type ) {

        synchronized( lock ) {
            if( _type.getCache() != useCache ) {
                String msg = MessageFormat.
                        format( "Requested cache object {0} with wrong cache type: {1} instead of {2}", _id, _type.getCache(), useCache );
                LOG.error( msg );
                throw new HandlerIllegalArgumentException( msg );
            }

            // return null if there was no entry for this key, otherwise returns the entry and puts it at the head of the list...
            return cache.get( _id );
        }
    }


    public void add( final BlogObject _obj ) {

        synchronized( lock ) {
            if( _obj.getType().getCache() != useCache ) {
                String msg = MessageFormat.format(
                        "Attempted to add object {0} with wrong cache type: {1} instead of {2}",
                        _obj.getBlogID(), _obj.getType().getCache(), useCache );
                LOG.error( msg );
                throw new HandlerIllegalArgumentException( msg );
            }

            // refuse to add any object that hasn't been resolved to bytes...
            // we don't want to wait for the read from inside a synchronized method...
            BlogObjectContent content = _obj.getContent();
            if( !(content instanceof BytesObjectContent) ) {
                LOG.warn( "Attempted to add object {0}.{1} that wasn't resolved to bytes", _obj.getBlogID(), _obj.getType().getCache() );
                return;
            }

            // if we don't have room in the cache, make some by removing the least recently used items until we have enough space...
            if( maxSize < content.memorySize() + currentSize ) {

                // iterate over our least recently used entries, removing them, until we have enough space for the new entry...
                Iterator<Map.Entry<BlogID,BlogObject>> it = cache.entrySet().iterator();
                while( it.hasNext() && (maxSize < content.memorySize() + currentSize) ) {

                    BlogObject loser = it.next().getValue();
                    currentSize -= loser.getContent().memorySize();
                    it.remove();
                }
            }

            // ok, now we can finally add it...
            cache.put( _obj.getBlogID(), _obj );
            currentSize += content.memorySize();
        }
    }


    public void remove( final BlogID _id, final BlogObjectType _type ) {

        synchronized( lock ) {
            if( _type.getCache() != useCache ) {
                String msg = MessageFormat.
                        format( "Attempted to remove cache object {0} with wrong cache type: {1} instead of {2}", _id, _type.getCache(), useCache );
                LOG.error( msg );
                throw new HandlerIllegalArgumentException( msg );
            }

            BlogObject obj = cache.remove( _id );
            currentSize -= obj.getContent().memorySize();
        }
    }
}
