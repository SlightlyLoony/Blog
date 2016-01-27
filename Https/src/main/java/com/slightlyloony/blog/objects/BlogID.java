package com.slightlyloony.blog.objects;

import com.google.common.base.Objects;
import com.google.gson.*;
import com.slightlyloony.blog.util.ID;

import java.lang.reflect.Type;

/**
 * Contains a 10 character string that is guaranteed to be a semantically valid blog IntegerDatum.  No guarantee is made about whether the IntegerDatum refers to an
 * actual object on the blog.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogID {

    private final String id;


    private BlogID( final String _id ) {
        id = _id;
    }


    public static BlogID create( final String _id ) {
        return ( (_id != null) && (_id.length() == 10) && ID.isValid( _id )) ? new BlogID( _id ) : null;
    }


    public long asLong() {
        return ID.decode( id );
    }


    @Override
    public boolean equals( final Object o ) {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;
        BlogID blogID = (BlogID) o;
        return Objects.equal( id, blogID.id );
    }


    @Override
    public int hashCode() {
        return Objects.hashCode( id );
    }


    public String getID() {
        return id;
    }


    @Override
    public String toString() {
        return id;
    }


    public static class Deserializer implements JsonDeserializer<BlogID> {

        @Override
        public BlogID deserialize( final JsonElement _jsonElement, final Type _type, final JsonDeserializationContext _jsonDeserializationContext )
                throws JsonParseException {

            if( !_jsonElement.isJsonPrimitive() )
                throw new JsonParseException( "Expected string, got something else" );

            return BlogID.create( _jsonElement.getAsString() );
        }
    }


    public static class Serializer implements JsonSerializer<BlogID> {


        @Override
        public JsonElement serialize( final BlogID _id, final Type _type, final JsonSerializationContext _context ) {
           return new JsonPrimitive( _id.id );
        }
    }
}
