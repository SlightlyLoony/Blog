package com.slightlyloony.blog.objects;

import com.google.common.collect.Maps;
import com.slightlyloony.blog.storage.BlogContentObjectCodec;
import com.slightlyloony.blog.storage.BlogObjectUseCache;
import com.slightlyloony.blog.storage.StorageCodec;
import com.slightlyloony.blog.users.UserCodec;
import com.slightlyloony.blog.users.UsersCodec;

import java.util.Map;

import static com.slightlyloony.blog.storage.BlogObjectUseCache.*;

/**
 * Defines all the possible types of blog objects, along with their file extensions and mime types.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum BlogObjectType {

    METADATA  ( "meta",   null,                                   META,   true,  new BlogObjectMetadataCodec() ),
    JPG       ( "jpg",    "image/jpeg",                           IMAGE,  false, new BlogContentObjectCodec()  ),
    PNG       ( "png",    "image/png",                            IMAGE,  false, new BlogContentObjectCodec()  ),
    GIF       ( "gif",    "image/gif",                            IMAGE,  false, new BlogContentObjectCodec()  ),
    ICO       ( "ico",    "image/x-icon",                         IMAGE,  false, new BlogContentObjectCodec()  ),
    HTML      ( "html",   "text/html",                            TEXT,   true,  new BlogContentObjectCodec()  ),
    CSS       ( "css",    "text/css",                             TEXT,   true,  new BlogContentObjectCodec()  ),
    JSON      ( "json",   "application/json",                     TEXT,   true,  new BlogContentObjectCodec()  ),
    JS        ( "js",     "application/javascript",               SCRIPT, true,  new BlogContentObjectCodec()  ),
    TXT       ( "txt",    "text/plain",                           TEXT,   true,  new BlogContentObjectCodec()  ),
    DOC       ( "doc",    "application/msword",                   NONE,   true,  new BlogContentObjectCodec()  ),
    PDF       ( "pdf",    "application/pdf",                      NONE,   false, new BlogContentObjectCodec()  ),
    XLS       ( "xls",    "application/vnd.ms-excel",             NONE,   true,  new BlogContentObjectCodec()  ),
    ZIP       ( "zip",    "application/zip",                      NONE,   false, new BlogContentObjectCodec()  ),
    SVG       ( "svg",    "image/svg+xml",                        IMAGE,  true,  new BlogContentObjectCodec()  ),
    XML       ( "xml",    "application/xml",                      TEXT,   true,  new BlogContentObjectCodec()  ),
    MP3       ( "mp3",    "audio/mpeg",                           BINARY, false, new BlogContentObjectCodec()  ),
    AAC       ( "aac",    "audio/mp4",                            BINARY, false, new BlogContentObjectCodec()  ),
    KMZ       ( "kmz",    "application/vnd.google-earth.kmz",     BINARY, true,  new BlogContentObjectCodec()  ),
    KML       ( "kml",    "application/vnd.google-earth.kml+xml", TEXT,   true,  new BlogContentObjectCodec()  ),
    XHTML     ( "xhtml",  "application/xhtml+xml",                TEXT,   true,  new BlogContentObjectCodec()  ),
    USERDATA  ( "user",   null,                                   USER,   false, new UserCodec()               ),
    USERINDEX ( "users",  null,                                   USER,   false, new UsersCodec()              );


    private static Map<String,BlogObjectType> EXTENSION_MAP;
    private static Map<String,BlogObjectType> MIME_MAP;


    private String extension;
    private String mime;
    private BlogObjectUseCache cache;
    private boolean compressible;
    private StorageCodec codec;


    BlogObjectType( final String _extension, final String _mime, final BlogObjectUseCache _cache,
                    final boolean _compressible, final StorageCodec _codec ) {

        extension = _extension;
        mime = _mime;
        cache = _cache;
        compressible = _compressible;
        codec = _codec;
        map( _extension, _mime );
    }


    public static BlogObjectType fromExtension( final String _extension ) {
        return EXTENSION_MAP.get( _extension );
    }


    public static BlogObjectType fromMime( final String _mime ) {
        return MIME_MAP.get( _mime );
    }


    public String getExtension() {
        return extension;
    }


    public String getMime() {
        return mime;
    }


    public BlogObjectUseCache getCache() {
        return cache;
    }


    public boolean isCompressible() {
        return compressible;
    }


    public StorageCodec getCodec() {
        return codec;
    }


    @Override
    public String toString() {
        return name();
    }


    private void map( final String _extension, final String _mime ) {

        if( EXTENSION_MAP == null )
            EXTENSION_MAP = Maps.newHashMap();
        EXTENSION_MAP.put( _extension, this );

        if( MIME_MAP == null )
            MIME_MAP = Maps.newHashMap();
        MIME_MAP.put( _mime, this );
    }
}
