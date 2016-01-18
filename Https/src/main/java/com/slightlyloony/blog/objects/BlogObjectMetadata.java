package com.slightlyloony.blog.objects;

import com.google.gson.*;
import com.slightlyloony.blog.handlers.RequestMethod;
import com.slightlyloony.blog.responders.Responder;
import com.slightlyloony.blog.responders.ResponderType;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Contains all the information needed for the blog server to be able to process a request.  This information is persisted in the blog storage
 * system as JSON files (file extension ".meta"), and methods are included to encode and decode objects of this class.
 *
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogObjectMetadata {

    private boolean browserCacheable;
    private boolean serverCacheable;
    private boolean mayCompress;
    private Map<RequestMethod,ResponderType> methods;
    private BlogID content;
    private BlogObjectType type;


    public BlogObjectMetadata() {
        browserCacheable = true;
        serverCacheable = true;
        mayCompress = true;
        methods = null;
        content = null;
        type = null;
    }


    public static BlogObjectMetadata create( final String _json ) {

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( BlogID.class, new BlogIDDeserializer() );
        Gson gson = gsonBuilder.create();

        return gson.fromJson( _json, BlogObjectMetadata.class );
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


    public boolean isBrowserCacheable() {
        return browserCacheable;
    }


    public boolean isServerCacheable() {
        return serverCacheable;
    }


    public boolean mayCompress() {
        return mayCompress;
    }


    public BlogID getContent() {
        return content;
    }


    public BlogObjectType getType() {
        return type;
    }


    public Responder getResponder( final RequestMethod _requestMethod ) {
        ResponderType responderType = methods.get( _requestMethod );
        return (responderType == null) ? null : responderType.getResponder();
    }
}
