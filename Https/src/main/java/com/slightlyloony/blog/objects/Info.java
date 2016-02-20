package com.slightlyloony.blog.objects;

import com.google.gson.*;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.security.BlogUserRights;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.S;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * A map of info keywords to info text.  Used by the info icons (little "i" in a blue circle) to provide information on selected topics.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Info extends BlogObjectObject {

    private static final Logger LOG = LogManager.getLogger();

    private Map<String,String> info;
    private Integer size;


    @Override
    public int size() {

        if( size != null )
            return size;

        int result = baseSize();
        for( Map.Entry<String,String> entry : info.entrySet() ) {
            result += 40;  // pointers, estimated hash overhead...
            result += S.strByteSize( entry.getKey() );
            result += S.strByteSize( entry.getValue() );
        }
        result += 8;  // size cache...

        size = result;

        return result;
    }


    public String get( final String _key ) {
        if( _key == null )
            return "";
        String result = info.get( _key );
        return (result == null) ? "" : result;
    }


    public static Info fromJSON( final String _json, final BlogID _id, final BlogObjectType _type,
                                 final BlogObjectAccessRequirements _accessRequirements ) throws StorageException {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( Info.class,           new Deserializer( _id, _type, _accessRequirements ) );
            Gson gson = gsonBuilder.create();
            return gson.fromJson( _json, Info.class );
        }
        catch( JsonSyntaxException e ) {
            throw new StorageException( "Problem deserializing User from JSON", e );
        }
    }


    public String toJSON() throws StorageException {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( Info.class, new Serializer() );
            Gson gson = gsonBuilder.create();
            return gson.toJson( this, getClass() );
        }
        catch( Exception e ) {
            throw new StorageException( "Problem serializing User to JSON", e );
        }
    }


    private static class Deserializer implements JsonDeserializer<Info> {

        private final BlogID id;
        private final BlogObjectType type;
        private final BlogObjectAccessRequirements accessRequirements;


        public Deserializer( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements ) {
            id = _id;
            type = _type;
            accessRequirements = _accessRequirements;
        }

        @Override
        public Info deserialize( final JsonElement _jsonElement, final Type _type, final JsonDeserializationContext _jsonDeserializationContext )
                throws JsonParseException {

            // first we deserialize it...
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter( BlogUserRights.class, new BlogUserRights.Deserializer()                   );
            Gson gson = gsonBuilder.create();
            Info result = gson.fromJson( _jsonElement, Info.class );

            // then we set the fields from our file name...
            result.type = type;
            result.blogID = id;
            result.accessRequirements = accessRequirements;

            return result;
        }
    }


    private static class Serializer implements JsonSerializer<Info> {

        @Override
        public JsonElement serialize( final Info _info, final Type _type, final JsonSerializationContext _jsonSerializationContext ) {

            JsonElement element = _jsonSerializationContext.serialize( _info, Info.class );
            _info.stripFields( element );
            return element;
        }
    }
}
