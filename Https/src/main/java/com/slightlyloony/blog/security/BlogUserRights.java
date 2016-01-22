package com.slightlyloony.blog.security;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogUserRights {

    private static final Logger LOG = LogManager.getLogger();

    public Set<BlogAccessRight> rights;

    public BlogUserRights() {
        rights = Sets.newHashSet();
    }


    public synchronized void add( final BlogAccessRight _right ) {

        if( _right == null )
            throw new HandlerIllegalArgumentException( "Missing the BlogAccessRight to add" );

        rights.add( _right );
    }


    public synchronized boolean has( final BlogAccessRight _right ) {

        if( _right == null )
            throw new HandlerIllegalArgumentException( "Missing the BlogAccessRight to test" );

        return rights.contains( _right );
    }


    /*
     * We serialize this class as if it were an array of strings, with each string representing a right.  So, for example:
     *
     *     ["MANAGER","AUTHOR"]
     *
     * would represent a collection of two rights: MANAGER and AUTHOR.
     */

    public static class Deserializer implements JsonDeserializer<BlogUserRights> {

        @Override
        public BlogUserRights deserialize( final JsonElement _jsonElement,
                                  final Type _type,
                                  final JsonDeserializationContext _jsonDeserializationContext ) throws JsonParseException {

            // iterate over all the rights in the JSON file...
            if( !_jsonElement.isJsonArray() )
                throw new JsonParseException( "Expected array start" );

            BlogUserRights result = new BlogUserRights();

            JsonArray usersArray = _jsonElement.getAsJsonArray();
            for( final JsonElement element : usersArray ) {

                // make sure we got a primitive value...
                if( !element.isJsonPrimitive() )
                    throw new JsonParseException( "Expected primitive for rights entry" );

                String rightName = element.getAsString();

                // make sure our string is actually a valid right...
                BlogAccessRight right;
                try {
                    right = BlogAccessRight.valueOf( rightName );
                }
                catch( IllegalArgumentException e ) {
                    throw new JsonParseException( "Invalid access right name: " + rightName, e );
                }

                // well, now we can add that right to our collection...
                result.add( right );
            }

            return result;
        }
    }


    public static class Serializer implements JsonSerializer<BlogUserRights> {


        @Override
        public JsonElement serialize( final BlogUserRights _users, final Type _type, final JsonSerializationContext _context ) {

            JsonArray result = new JsonArray();

            // iterate over all the entries...
            for( BlogAccessRight right : _users.rights ) {
                result.add( right.toString() );
            }

            return result;
        }
    }
}
