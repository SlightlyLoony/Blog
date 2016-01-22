package com.slightlyloony.blog.objects;

import com.google.common.collect.Maps;
import com.slightlyloony.blog.storage.BlogObjectUseCache;

import java.util.Map;

import static com.slightlyloony.blog.storage.BlogObjectUseCache.*;

/**
 * Defines all the possible types of blog objects, along with their file extensions and mime types.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum BlogObjectType {

    METADATA  ( "meta",   "",                                     META,   true  ),
    JPG       ( "jpg",    "image/jpeg",                           IMAGE,  false ),
    PNG       ( "png",    "image/png",                            IMAGE,  false ),
    GIF       ( "gif",    "image/gif",                            IMAGE,  false ),
    ICO       ( "ico",    "image/x-icon",                         IMAGE,  false ),
    HTML      ( "html",   "text/html",                            TEXT,   true  ),
    CSS       ( "css",    "text/css",                             TEXT,   true  ),
    JSON      ( "json",   "application/json",                     TEXT,   true  ),
    JS        ( "js",     "application/javascript",               SCRIPT, true  ),
    TXT       ( "txt",    "text/plain",                           TEXT,   true  ),
    DOC       ( "doc",    "application/msword",                   NONE,   true  ),
    PDF       ( "pdf",    "application/pdf",                      NONE,   false ),
    XLS       ( "xls",    "application/vnd.ms-excel",             NONE,   true  ),
    ZIP       ( "zip",    "application/zip",                      NONE,   false ),
    SVG       ( "svg",    "image/svg+xml",                        IMAGE,  true  ),
    XML       ( "xml",    "application/xml",                      TEXT,   true  ),
    MP3       ( "mp3",    "audio/mpeg",                           BINARY, false ),
    AAC       ( "aac",    "audio/mp4",                            BINARY, false ),
    KMZ       ( "kmz",    "application/vnd.google-earth.kmz",     BINARY, true  ),
    KML       ( "kml",    "application/vnd.google-earth.kml+xml", TEXT,   true  ),
    XHTML     ( "xhtml",  "application/xhtml+xml",                TEXT,   true  ),
    USERDATA  ( "user",   "",                                     USER,   true  );


    private static Map<String,BlogObjectType> EXTENSION_MAP;
    private static Map<String,BlogObjectType> MIME_MAP;


    private String extension;
    private String mime;
    private BlogObjectUseCache cache;
    private boolean compressible;


    BlogObjectType( final String _extension, final String _mime, final BlogObjectUseCache _cache, final boolean _compressible ) {
        extension = _extension;
        mime = _mime;
        cache = _cache;
        compressible = _compressible;
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


    @Override
    public String toString() {
        return extension;
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
