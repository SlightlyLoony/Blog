package com.slightlyloony.blog.objects;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.slightlyloony.blog.handlers.RequestMethod;
import com.slightlyloony.blog.responders.Responder;
import com.slightlyloony.blog.responders.ResponderType;

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
public class BlogObjectMetadata {

    private boolean browserCacheable;
    private boolean serverCacheable;
    private ContentCompressionState compressionState;
    private Map<RequestMethod,ResponderType> methods;
    private BlogID content;
    private BlogObjectType type;


    public BlogObjectMetadata() {
        browserCacheable = true;
        serverCacheable = true;
        compressionState = UNCOMPRESSED;
        methods = Maps.newHashMap();
        content = null;
        type = null;
    }


    public static BlogObjectMetadata create( final String _json ) {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( BlogID.class, new BlogIDDeserializer() );
        gsonBuilder.registerTypeAdapter( BlogObjectMetadata.class, new MetadataSerializer() );
        Gson gson = gsonBuilder.create();

        return gson.fromJson( _json, BlogObjectMetadata.class );
    }


    public boolean isBrowserCacheable() {
        return browserCacheable;
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


    public BlogObjectType getType() {
        return type;
    }


    public void setBrowserCacheable( final boolean _browserCacheable ) {
        browserCacheable = _browserCacheable;
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


    public void setType( final BlogObjectType _type ) {
        type = _type;
    }


    public Responder getResponder( final RequestMethod _requestMethod ) {
        ResponderType responderType = methods.get( _requestMethod );
        return (responderType == null) ? null : responderType.getResponder();
    }


    private static class BlogIDDeserializer implements JsonDeserializer<BlogID> {

        @Override
        public BlogID deserialize( final JsonElement _jsonElement,
                                   final Type _type,
                                   final JsonDeserializationContext _jsonDeserializationContext )
                throws JsonParseException {

            return BlogID.create( _jsonElement.getAsString() );
        }
    }


    public static class MetadataSerializer implements JsonSerializer<BlogObjectMetadata> {


        @Override
        public JsonElement serialize( final BlogObjectMetadata _metadata, final Type _type,
                                      final JsonSerializationContext _context ) {

            JsonObject result = new JsonObject();

            // only add those fields that DON'T have their default value...
            if( !_metadata.browserCacheable )                result.addProperty( "browserCacheable", false );
            if( !_metadata.serverCacheable )                 result.addProperty( "serverCacheable",  false );
            if( _metadata.compressionState != UNCOMPRESSED ) result.add( "compressionState", _context.serialize( _metadata.compressionState ) );
            if( _metadata.methods.size() > 0 )               result.add( "methods",          _context.serialize( _metadata.methods          ) );
            if( _metadata.content != null )                  result.add( "content",          _context.serialize( _metadata.content          ) );
            if( _metadata.type != null )                     result.add( "type",             _context.serialize( _metadata.type             ) );

            return result;
        }
    }
}
