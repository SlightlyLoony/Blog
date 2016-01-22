package com.slightlyloony.blog.users;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.config.BlogConfig;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.security.BlogUserRights;
import com.slightlyloony.blog.storage.BlogObjectIterator;
import com.slightlyloony.blog.storage.BlogObjectIterator.BlogObjectInfo;
import com.slightlyloony.blog.util.ID;
import com.slightlyloony.blog.util.S;
import com.slightlyloony.blog.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import static com.slightlyloony.blog.util.S.fromUTF8;
import static com.slightlyloony.blog.util.S.toUTF8;
import static com.slightlyloony.common.logging.LU.msg;

/**
 * Index for the users of a blog.  There are actually three indexes: two forward indexes (one by username, the other by user cookie value for
 * persistent logins) and one reverse (needed in order to maintain the index).  If this class is instantiated for a blog with NO users, a default
 * manager user is created automagically.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Users {

    private static final Logger LOG = LogManager.getLogger();

    /*
     * These three structures use byte[] instances to hold the UTF-8 encoded string values that would otherwise be in String instances.  By encoding
     * them into byte arrays we (roughly) cut the memory consumed by 50%.
     */
    private final Map<byte[],byte[]> byUsername;
    private final Map<byte[],byte[]> byCookie;
    private final Map<byte[],Keys> reverse;


    private Users() {
        byUsername = Maps.newHashMap();
        byCookie = Maps.newHashMap();
        reverse = Maps.newHashMap();
    }


    public static Users create( final BlogConfig _blogConfig ) {

        // get the id of our users file...
        BlogID id = BlogID.create( _blogConfig.getUsers() );

        // if we don't have a users file configured, it's time to make a default one...
        // we'll make a default manager user, the rest will come from the recovery code...
        if( id == null )
            makeDefaultUserFile( _blogConfig.getDomain() );

        // otherwise, read the users file...
        BlogObject object = null;
        if( id != null )
            object = BlogServer.STORAGE.read( id, BlogObjectType.JSON, null, ContentCompressionState.UNCOMPRESSED );

        // if we failed to read the index, recover it the hard way...
        if( (object == null) || !object.isValid() ) {
            LOG.error( "Could not read users file; rebuilding from user data" );
            return initializeFromUserFiles( _blogConfig );
        }

        // deserialize this beast with our custom deserializer...
        try {
            return getGson().fromJson( object.getUTF8String(), Users.class );
        }
        catch( JsonParseException e ) {
            LOG.error( "Problem parsing JSON in users record: " + id, e );
            return null;
        }
    }


    // TODO: lazy write of index file...
    public synchronized void indexUser( final BlogID _id, final User _user ) {

        if( (_id == null) || (_user == null) )
            throw new HandlerIllegalArgumentException( "Blog ID or user missing" );

        byte[] blogID = toUTF8( _id.getID() );
        byte[] username = toUTF8( _user.getUsername() );
        byte[] cookieValue = toUTF8( _user.getCookie() );

        // first we get the keys to any existing entries for this user, as both the username and session cookie values might have changed...
        // we use the blog ID (which is invariant for any given user) to look up the keys for the other two maps...
        Keys keys = reverse.get( blogID );

        // if we got no entry for the keys, then we've never indexed this user before - just add him...
        if( keys == null ) {
            keys = new Keys( username, cookieValue );
            reverse.put( blogID, keys );
            if( keys.username != null )
                byUsername.put( keys.username, blogID );
            if( keys.cookieValue != null )
                byCookie.put( keys.cookieValue, blogID );
        }

        // if both keys are unchanged, we've got nothing to do here...
        if( Arrays.equals( keys.username, username ) || Arrays.equals( keys.cookieValue, cookieValue ) )
            return;

        // if the username has changed, update it...
        if( keys.username != null )
            blogID = byUsername.remove( keys.username );  // this updates blogID to the original instance stored...
        keys.username = username;
        if( username != null )
            byUsername.put( username, blogID );

        // if the cookie value has changed, update it...
        if( keys.cookieValue != null )
            blogID = byCookie.remove( keys.cookieValue );  // this updates blogID to the original instance stored...
        keys.cookieValue = cookieValue;
        if( cookieValue != null )
            byCookie.put( cookieValue, blogID );
    }


    public synchronized User getUserFromUsername( final String _username ) {

        if( _username == null )
            throw new HandlerIllegalArgumentException( "Missing username" );

        return readUser( byUsername.get( toUTF8( _username ) ) );
    }


    public synchronized User getUserFromCookie( final String _cookieValue ) {

        if( _cookieValue == null )
            throw new HandlerIllegalArgumentException( "Missing cookie value" );

        return readUser( byCookie.get( toUTF8( _cookieValue ) ) );
    }


    private User readUser( final byte[] _userIDBytes ) {

        if( _userIDBytes == null )
            return null;

        BlogID userID = BlogID.create( fromUTF8( _userIDBytes ) );
        if( userID == null )
            return null;

        BlogObject object = BlogServer.STORAGE.read( userID, BlogObjectType.USERDATA, null, ContentCompressionState.UNCOMPRESSED );
        if( !object.isValid() )
            return null;

        return User.create( object );
    }


    /**
     * Creates a default manager user, stored at a new ID.
     *
     * @param _blogName the name of the blog this user belongs to
     */
    private static void makeDefaultUserFile( final String _blogName ) {

        // make our synthetic user...
        User user = new User();
        user.setBlog( _blogName );
        user.setUsername( "manager" );
        user.setPasswordHashedAndSalted( BCrypt.hashpw( "blog", BCrypt.gensalt() ) );
        BlogUserRights rights = new BlogUserRights();
        rights.add( BlogAccessRight.MANAGER );
        rights.add( BlogAccessRight.ADULT );
        rights.add( BlogAccessRight.AUTHOR );
        rights.add( BlogAccessRight.REVIEWER );
        user.setRights( rights );

        // then serialize it...
        user.update( null );
    }


    /**
     * This method is invoked when there's a failure (for whatever reason) to read the users index file.  It will initialize the users instance by
     * iterating over <i>all</i> the files in the blog (potentially a lengthy process).  Once that's complete, it will serialize the instance back
     * to the users index file, to recreate it.
     *
     * @return the newly created users instance
     */
    private static Users initializeFromUserFiles( final BlogConfig _blogConfig ) {

        Timer t = new Timer();
        LOG.info( "Creating Users instance the hard way: reading all blog files" );

        Users result = new Users();

        // iterate over all the blog object files...
        BlogObjectIterator it = new BlogObjectIterator();
        while( it.hasNext() ) {

            BlogObjectInfo info = it.next();

            // if we don't have a user data file, just skip it...
            if( !info.file.getName().endsWith( ".user" ) )
                continue;

            // read the user data in and instantiate it...
            BlogObject object = BlogServer.STORAGE.read( info.id, BlogObjectType.USERDATA, null, ContentCompressionState.UNCOMPRESSED );
            if( !object.isValid() ) {
                LOG.error( "Can't read user: ID " + info.id );
                continue;
            }
            User user = User.create( object );
            if( user == null ) {
                LOG.error( "Can't deserialize user: ID " + info.id );
                continue;
            }

            // if this user doesn't belong to this blog, skip it...
            if( !user.getBlog().equals( _blogConfig.getDomain() ))
                continue;

            // index this user...
            result.indexUser( info.id, user );
        }

        // get the blog ID for this record, or null if none has been assigned yet...
        BlogID usersID = BlogID.create( _blogConfig.getUsers() );

        // now write the users out to disk...
        BlogObject object = result.update( usersID );

        // if we created the record (vs. just updating it), then we need to update the blog configuration...
        if( usersID == null ) {
            _blogConfig.setUsers( object.getBlogID().getID() );
            _blogConfig.serialize();
        }

        t.mark();
        LOG.info( msg( "Completed creating Users instance by reading file system in {0}", t.toString() ) );

        return result;
    }


    /**
     * Writes this index out to a serialized file.  If the blog has not yet assigned an ID for this, the ID will be automatically assigned.
     *
     * @param _id the blog ID for this record, or null if it is unknown (in which case it is created).
     */
    public BlogObject update( final BlogID _id ) {

        // get our json content...
        String json = getGson().toJson( this, Users.class );
        byte[] bytes = S.toUTF8( json );
        BytesObjectContent content = new BytesObjectContent( bytes, ContentCompressionState.UNCOMPRESSED, bytes.length );

        // store it...
        if( _id == null )
            return BlogServer.STORAGE.create( content, BlogObjectType.JSON, null, ContentCompressionState.UNCOMPRESSED );
        else
            return BlogServer.STORAGE.modify( new BlogObject( _id, BlogObjectType.JSON, null, content ) );
    }


    private static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter( Users.class, new UsersDeserializer() );
        gsonBuilder.registerTypeAdapter( Users.class, new UsersSerializer() );
        return gsonBuilder.create();
    }


    private static class Keys {
        private byte[] username;
        private byte[] cookieValue;


        public Keys( final byte[] _username, final byte[] _cookieValue ) {
            username = _username;
            cookieValue = _cookieValue;
        }
    }


    /*
     * We persist this index of users with a JSON array (with one entry per user) of three element string arrays (blog ID, username, and cookie
     * value).  For example, a file with two users might look something like this:
     *
     * [
     *    ["AAAAAAABCD", "HamburgerHelper", "LuRdWoHieDkUsoX-_iZw"],
     *    ["AAAAAABu_Q", "bozo@circus.com", null]
     * ]
     */


    private static class UsersDeserializer implements JsonDeserializer<Users> {

        @Override
        public Users deserialize( final JsonElement _jsonElement,
                                  final Type _type,
                                  final JsonDeserializationContext _jsonDeserializationContext ) throws JsonParseException {

            // iterate over all the users in the JSON file...
            if( !_jsonElement.isJsonArray() )
                throw new JsonParseException( "Expected array start" );

            Users result = new Users();

            JsonArray usersArray = _jsonElement.getAsJsonArray();
            for( final JsonElement element : usersArray ) {

                // make sure we got a three-element array...
                if( !element.isJsonArray() )
                    throw new JsonParseException( "Expected array start for user entry" );

                JsonArray userArray = element.getAsJsonArray();
                if( userArray.size() != 3 )
                    throw new JsonParseException( "Expected 3 elements in array for user entry" );

                // extract the index elements for one user...
                String id = userArray.get( 0 ).getAsString();
                String username = userArray.get( 1 ).getAsString();
                String cookie = userArray.get( 2 ).isJsonNull() ? null : userArray.get( 2 ).getAsString();

                if( !ID.isValid(id) || (username == null) )
                    throw new JsonParseException( "Invalid values in id or username" );

                // update the indexes...
                byte[] idKey = toUTF8( id );
                byte[] usernameKey = toUTF8( username );
                byte[] cookieKey = toUTF8( cookie );
                Keys keys = new Keys( usernameKey, cookieKey );
                result.reverse.put( idKey, keys );
                result.byUsername.put( usernameKey, idKey );
                if( cookieKey != null )
                    result.byCookie.put( cookieKey, idKey );
            }

            return result;
        }
    }


    public static class UsersSerializer implements JsonSerializer<Users> {


        @Override
        public JsonElement serialize( final Users _users, final Type _type,
                                      final JsonSerializationContext _context ) {

            JsonArray result = new JsonArray();

            // iterate over all the entries in the reverse index, as they have all the data we need...
            for( Map.Entry<byte[],Keys> entry : _users.reverse.entrySet() ) {

                JsonArray item = new JsonArray();
                item.add( fromUTF8( entry.getKey() ) );
                item.add( fromUTF8( entry.getValue().username ) );
                item.add( fromUTF8( entry.getValue().cookieValue ) );
                result.add( item );
            }

            return result;
        }
    }
}
