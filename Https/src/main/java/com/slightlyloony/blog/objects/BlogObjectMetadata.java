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


    public void setUnauthorizedResponder( final ResponderType _unauthorizedResponder ) {
        unauthorizedResponder = _unauthorizedResponder;
    }


    public Responder getResponder( final RequestMethod _requestMethod ) {
        ResponderType responderType = methods.get( _requestMethod );
        return (responderType == null) ? null : responderType.getResponder();
    }


    public String toJSON() throws StorageException {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
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
            gsonBuilder.registerTypeAdapter( BlogID.class,             new BlogID.Deserializer()                           );
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

            if( externalCacheSecondsDiff )      result.addProperty( "externalCacheSeconds", _metadata.externalCacheSeconds );
            if( !_metadata.serverCacheable )    result.addProperty( "serverCacheable",      false );
            if( unauthorizedResponderDiff )     result.add( "unauthorizedResponder", _context.serialize( _metadata.unauthorizedResponder ) );
            if( compressionStateDiff )          result.add( "compressionState",      _context.serialize( _metadata.compressionState      ) );
            if( _metadata.methods.size() > 0 )  result.add( "methods",               _context.serialize( _metadata.methods               ) );
            if( _metadata.content != null )     result.add( "content",               _context.serialize( _metadata.content               ) );
            if( _metadata.contentType != null ) result.add( "contentType",           _context.serialize( _metadata.contentType           ) );
            if( _metadata.sourceType != null )  result.add( "sourceType",            _context.serialize( _metadata.sourceType            ) );

            return result;
        }
    }
}
