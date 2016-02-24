package com.slightlyloony.blog.objects;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.slightlyloony.blog.handlers.RequestMethod;
import com.slightlyloony.blog.responders.Responder;
import com.slightlyloony.blog.responders.ResponderType;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.templates.sources.SourceType;

import java.lang.reflect.Type;
import java.util.Map;

import static com.slightlyloony.blog.objects.ContentCompressionState.UNCOMPRESSED;

/**
 * Contains all the information needed for the blog server to be able to produce a response to a request, other than the actual content.  This
 * information is persisted in the blog storage system as JSON files (file extension ".meta"), and methods are included to encode and decode objects
 * of this class.  There is a custom serializer in order to provide one particular little bit of magic: fields with default values are completely
 * left out, in order to minimize the size of the on-disk object.  This is less for saving space (as each file will be 4k minimum anyway), and more
 * about minimizing the deserialization time.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObjectMetadata extends BlogObjectObject {

    private static final int DEFAULT_EXTERNAL_CACHE_SECONDS = 30 * 24 * 3600;

    private int externalCacheSeconds;
    private boolean serverCacheable;
    private ContentCompressionState compressionState;
    private Map<RequestMethod,ResponderType> methods;
    private ResponderType unauthorizedResponder;
    private BlogID content;
    private BlogObjectType contentType;
    private SourceType sourceType;

    // image-specific metadata...
    private int height;
    private int width;
    private int size;
    private String source;
    private String credit;
    private String description;
    private String title;
    private String where;
    private String when;
    private String cameraSettings;
    private String cameraOrientation;
    private ScaledImage[] scaledImages;


    public BlogObjectMetadata( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements ) {
        super( _id, _type, _accessRequirements );
        externalCacheSeconds = DEFAULT_EXTERNAL_CACHE_SECONDS;
        serverCacheable = true;
        compressionState = UNCOMPRESSED;
        methods = Maps.newHashMap();
        content = null;
        contentType = null;
        unauthorizedResponder = null;
    }


    private BlogObjectMetadata() {
        super();
        externalCacheSeconds = DEFAULT_EXTERNAL_CACHE_SECONDS;
        serverCacheable = true;
        compressionState = UNCOMPRESSED;
        methods = Maps.newHashMap();
        content = null;
        contentType = null;
        unauthorizedResponder = null;
    }


    @Override
    public int size() {
        int result = baseSize();

        // primitives...
        result += (8 + 8 + 8 + 20 + 8);

        // methods...
        result += methods.size() * 16 + 200;

        return result;
    }


    public SourceType getSourceType() {
        return sourceType;
    }


    public void setSourceType( final SourceType _sourceType ) {
        sourceType = _sourceType;
    }


    public int getExternalCacheSeconds() {
        return externalCacheSeconds;
    }


    public void setExternalCacheSeconds( final int _externalCacheSeconds ) {
        externalCacheSeconds = _externalCacheSeconds;
    }


    public boolean isServerCacheable() {
        return serverCacheable;
    }


    public ContentCompressionState getCompressionState() {
        return compressionState;
    }


    public BlogID getContent() {
        return content;
    }


    public BlogObjectType getContentType() {
        return contentType;
    }


    public void setServerCacheable( final boolean _serverCacheable ) {
        serverCacheable = _serverCacheable;
    }


    public void setCompressionState( final ContentCompressionState _compressionState ) {
        compressionState = _compressionState;
    }


    public void addMethod( final RequestMethod _requestMethod, final ResponderType _responderType ) {
        methods.put( _requestMethod, _responderType );
    }


    public ResponderType getMethod( final RequestMethod _requestMethod ) {
        return methods.get( _requestMethod );
    }


    public void setContent( final BlogID _content ) {
        content = _content;
    }


    public void setContentType( final BlogObjectType _contentType ) {
        contentType = _contentType;
    }


    public ResponderType getUnauthorizedResponder() {
        return unauthorizedResponder;
    }


    public String getSource() {
        return source;
    }


    public void setSource( final String _source ) {
        source = _source;
    }


    public String getCredit() {
        return credit;
    }


    public void setCredit( final String _credit ) {
        credit = _credit;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription( final String _description ) {
        description = _description;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle( final String _title ) {
        title = _title;
    }


    public String getWhere() {
        return where;
    }


    public void setWhere( final String _where ) {
        where = _where;
    }


    public String getWhen() {
        return when;
    }


    public void setWhen( final String _when ) {
        when = _when;
    }


    public String getCameraSettings() {
        return cameraSettings;
    }


    public void setCameraSettings( final String _cameraSettings ) {
        cameraSettings = _cameraSettings;
    }


    public String getCameraOrientation() {
        return cameraOrientation;
    }


    public void setCameraOrientation( final String _cameraOrientation ) {
        cameraOrientation = _cameraOrientation;
    }


    /**
     * Adds the given scaled image record in ascending order of height...
     *
     * @param _scaledImage the scaled image record to add...
     */
    public void add( final ScaledImage _scaledImage ) {
        if( scaledImages == null )
            scaledImages = new ScaledImage[] { _scaledImage } ;
        else {
            ScaledImage[] result = new ScaledImage[scaledImages.length + 1];
            int j = 0;
            int i = 0;
            for( ; i < scaledImages.length;  ) {
                if( (j == i) && (_scaledImage.height < scaledImages[i].height) )
                    result[j++] = _scaledImage;
                result[j++] = scaledImages[i++];
            }

            // if we still haven't stored the new one, it must be the biggest one...
            if( j == i ) {
                result[j] = _scaledImage;
            }
            scaledImages = result;
        }
    }


    public void setHeight( final int _height ) {
        height = _height;
    }


    public void setWidth( final int _width ) {
        width = _width;
    }


    public void setUnauthorizedResponder( final ResponderType _unauthorizedResponder ) {
        unauthorizedResponder = _unauthorizedResponder;
    }


    public int getHeight() {
        return height;
    }


    public int getWidth() {
        return width;
    }


    public ScaledImage[] getScaledImages() {
        return scaledImages;
    }


    public int getSize() {
        return size;
    }


    public void setSize( final int _size ) {
        size = _size;
    }


    public Responder getResponder( final RequestMethod _requestMethod ) {
        ResponderType responderType = methods.get( _requestMethod );
        return (responderType == null) ? null : responderType.getResponder();
    }


    public static class ScaledImage {

        public final BlogID content;
        public final int height;
        public final int width;


        public ScaledImage( final BlogID _content, final int _height, final int _width ) {
            content = _content;
            height = _height;
            width = _width;
        }
    }


    public String toJSON() throws StorageException {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( BlogID.class, new BlogID.Serializer() );
            gsonBuilder.registerTypeAdapter( BlogObjectMetadata.class, new Serializer()   );
            Gson gson = gsonBuilder.create();
            return gson.toJson( this, getClass() );
        }
        catch( Exception e ) {
            throw new StorageException( "Problem serializing BlogObjectMetadata to JSON", e );
        }
    }


    public static BlogObjectMetadata fromJSON( final String _json, final BlogID _id, final BlogObjectType _type,
                                               final BlogObjectAccessRequirements _accessRequirements ) throws StorageException {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( BlogObjectMetadata.class, new Deserializer( _id, _type, _accessRequirements ) );
            Gson gson = gsonBuilder.create();
            return gson.fromJson( _json, BlogObjectMetadata.class );
        }
        catch( JsonSyntaxException e ) {
            throw new StorageException( "Problem deserializing BlogObjectMetadata from JSON: " + e.getMessage(), e );
        }
    }


    private static class Deserializer implements JsonDeserializer {

        private final BlogID id;
        private final BlogObjectType type;
        private final BlogObjectAccessRequirements accessRequirements;


        public Deserializer( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements ) {
            id = _id;
            type = _type;
            accessRequirements = _accessRequirements;
        }


        @Override
        public BlogObjectMetadata deserialize( final JsonElement _jsonElement, final Type _type,
                                               final JsonDeserializationContext _jsonDeserializationContext ) throws JsonParseException {

            // first we deserialize it...
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( BlogID.class, new BlogID.Deserializer()                           );
            Gson gson = gsonBuilder.create();
            BlogObjectMetadata result = gson.fromJson( _jsonElement, BlogObjectMetadata.class );

            // then we set the fields from our file name...
            result.type = type;
            result.blogID = id;
            result.accessRequirements = accessRequirements;

            return result;
        }
    }


    private static class Serializer implements JsonSerializer<BlogObjectMetadata> {


        @Override
        public JsonElement serialize( final BlogObjectMetadata _metadata, final Type _type,
                                      final JsonSerializationContext _context ) {

            JsonObject result = new JsonObject();

            // only add those fields that DON'T have their default value...
            boolean externalCacheSecondsDiff = _metadata.externalCacheSeconds != DEFAULT_EXTERNAL_CACHE_SECONDS;
            boolean compressionStateDiff = _metadata.compressionState != UNCOMPRESSED;
            boolean unauthorizedResponderDiff = _metadata.unauthorizedResponder != null;

            if( externalCacheSecondsDiff )       result.addProperty( "externalCacheSeconds", _metadata.externalCacheSeconds );
            if( !_metadata.serverCacheable )     result.addProperty( "serverCacheable",      false );
            if( unauthorizedResponderDiff )      result.add( "unauthorizedResponder", _context.serialize( _metadata.unauthorizedResponder ) );
            if( compressionStateDiff )           result.add( "compressionState",      _context.serialize( _metadata.compressionState      ) );
            if( _metadata.methods.size() > 0 )   result.add( "methods",               _context.serialize( _metadata.methods               ) );
            if( _metadata.content != null )      result.add( "content",               _context.serialize( _metadata.content               ) );
            if( _metadata.contentType != null )  result.add( "contentType",           _context.serialize( _metadata.contentType           ) );
            if( _metadata.sourceType != null )   result.add( "sourceType",            _context.serialize( _metadata.sourceType            ) );

            // image-specific fields...
            if( _metadata.height != 0 )          result.add( "height",                _context.serialize( _metadata.height                ) );
            if( _metadata.width != 0 )           result.add( "width",                 _context.serialize( _metadata.width                 ) );
            if( _metadata.size != 0 )            result.add( "size",                  _context.serialize( _metadata.size                  ) );
            if( _metadata.scaledImages != null ) result.add( "scaledImages",          _context.serialize( _metadata.scaledImages          ) );

            return result;
        }
    }
}
