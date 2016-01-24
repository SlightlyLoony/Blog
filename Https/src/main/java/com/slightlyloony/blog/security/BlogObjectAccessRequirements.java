package com.slightlyloony.blog.security;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Enumerates all the possible combinations of the rights required for access to a blog object.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum BlogObjectAccessRequirements {

    PUBLIC         ( 'A', Sets.newHashSet( BlogAccessRight.PUBLIC                               ) ),
    SESSION        ( 'B', Sets.newHashSet( BlogAccessRight.SESSION                              ) ),
    AUTHENTICATED  ( 'C', Sets.newHashSet( BlogAccessRight.AUTHENTICATED                        ) ),  // implies SESSION...
    ADULT          ( 'D', Sets.newHashSet( BlogAccessRight.ADULT                                ) ),  // implies SESSION, AUTHENTICATED...
    REVIEWER       ( 'E', Sets.newHashSet( BlogAccessRight.REVIEWER                             ) ),  // implies SESSION, AUTHENTICATED...
    AUTHOR         ( 'F', Sets.newHashSet( BlogAccessRight.AUTHOR                               ) ),  // implies SESSION, AUTHENTICATED...
    ADULT_REVIEWER ( 'G', Sets.newHashSet( BlogAccessRight.ADULT, BlogAccessRight.REVIEWER      ) ),  // implies SESSION, AUTHENTICATED...
    ADULT_AUTHOR   ( 'H', Sets.newHashSet( BlogAccessRight.ADULT, BlogAccessRight.AUTHOR        ) ),  // implies SESSION, AUTHENTICATED...
    MANAGER        ( 'Z', Sets.newHashSet( BlogAccessRight.MANAGER                              ) ),  // implies SESSION, AUTHENTICATED...
    ADMIN          ( '0', Sets.newHashSet( BlogAccessRight.ADMIN                                ) );  // implies SESSION, AUTHENTICATED...


    private static Map<Character,BlogObjectAccessRequirements> MAP_CODE;

    private final char code;
    private final Set<BlogAccessRight> requirements;


    BlogObjectAccessRequirements( final char _code, final Set<BlogAccessRight> _requirements  ) {
        code = _code;
        requirements = _requirements;
        map( _code, this );
    }


    private static void map( final char _code, final BlogObjectAccessRequirements _accessRequirements ) {
        if( MAP_CODE == null )
            MAP_CODE = Maps.newHashMap();
        MAP_CODE.put( _code, _accessRequirements );
    }


    public static BlogObjectAccessRequirements get( final char _code ) {
        return MAP_CODE.get( _code );
    }


    public char getCode() {
        return code;
    }


    public Set<BlogAccessRight> getRequirements() {
        return requirements;
    }


    /**
     * Returns true if the given user has rights matching each requirement (i.e., all of them) in this instance.
     *
     * @param _userRights the user rights to test
     * @return true if the given user rights allow access to the blog object with the rights required in this instance
     */
    public boolean isAuthorized( final BlogUserRights _userRights ) {

        if( _userRights == null )
            throw new HandlerIllegalArgumentException( "User rights are null" );

        for( BlogAccessRight requirement : requirements ) {
            if( !_userRights.has( requirement ))
                return false;
        }

        return true;
    }


    public static class Deserializer implements JsonDeserializer<BlogObjectAccessRequirements> {

        @Override
        public BlogObjectAccessRequirements deserialize( final JsonElement _jsonElement, final Type _type, final JsonDeserializationContext _jsonDeserializationContext )
                throws JsonParseException {

            if( !_jsonElement.isJsonPrimitive() )
                throw new JsonParseException( "Expected string, got something else" );

            return BlogObjectAccessRequirements.get( _jsonElement.getAsString().charAt( 0 ) );
        }
    }


    public static class Serializer implements JsonSerializer<BlogObjectAccessRequirements> {


        @Override
        public JsonElement serialize( final BlogObjectAccessRequirements _accessRequirements, final Type _type, final JsonSerializationContext _context ) {
            return new JsonPrimitive( _accessRequirements.getCode() );
        }
    }
}
