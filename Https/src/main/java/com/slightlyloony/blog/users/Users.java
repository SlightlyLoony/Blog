package com.slightlyloony.blog.users;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.config.BlogConfig;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.objects.*;
import com.slightlyloony.blog.storage.BlogObjectIterator;
import com.slightlyloony.blog.storage.BlogObjectIterator.BlogObjectInfo;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.util.ID;
import com.slightlyloony.blog.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import static com.slightlyloony.blog.security.BlogAccessRight.*;
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
public class Users extends BlogObjectObject {

    private static final Logger LOG = LogManager.getLogger();

    /*
     * These three structures use byte[] instances to hold the UTF-8 encoded string values that would otherwise be in String instances.  By encoding
     * them into byte arrays we (roughly) cut the memory consumed by 50%.
     */
    private final Map<byte[],byte[]> byUsername;
    private final Map<byte[],byte[]> byCookie;
    private final Map<byte[],Keys> reverse;




    private Users( final BlogID _id ) {
        super( _id, BlogObjectType.USERINDEX, null );

        byUsername = Maps.newHashMap();
        byCookie = Maps.newHashMap();
        reverse = Maps.newHashMap();
    }


    private Users() {
        super();
        byUsername = Maps.newHashMap();
        byCookie = Maps.newHashMap();
        reverse = Maps.newHashMap();
    }


    public int size() {
        int result = super.size();
        result += reverse.size() * (40 + 10) + 1000;
        result += byCookie.size() * (20 + 10) + 1000;
        result += byUsername.size() * (20 + 10) + 1000;
        return result;
    }


    public static Users create( final BlogConfig _blogConfig ) throws StorageException {

        // get the id of our users file...
        BlogID id = BlogID.create( _blogConfig.getUsers() );

        // if we don't have a users file configured, it's time to make a default one...
        // we'll make a default manager user, the rest will come from the recovery code...
        if( id == null )
            makeDefaultUserFile( _blogConfig.getDomain() );

        // otherwise, read the users file...
        BlogObject object = null;

        try {
            if( id != null )
                object = BlogServer.STORAGE.read( id, BlogObjectType.USERINDEX, null, ContentCompressionState.UNCOMPRESSED );
        }
        catch( StorageException e ) {
            LOG.error( "Could not read users file; rebuilding from user data" );
        }

        return (object == null) ? initializeFromUserFiles( _blogConfig ) : (Users) object;
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


    public synchronized User getUserFromUsername( final String _username ) throws StorageException {

        if( _username == null )
            throw new HandlerIllegalArgumentException( "Missing username" );

        return readUser( byUsername.get( toUTF8( _username ) ) );
    }


    public synchronized User getUserFromCookie( final String _cookieValue ) throws StorageException {

        if( _cookieValue == null )
            throw new HandlerIllegalArgumentException( "Missing cookie value" );

        return readUser( byCookie.get( toUTF8( _cookieValue ) ) );
    }


    private User readUser( final byte[] _userIDBytes ) throws StorageException {

        if( _userIDBytes == null )
            return null;

        BlogID userID = BlogID.create( fromUTF8( _userIDBytes ) );
        if( userID == null )
            return null;

        return (User) BlogServer.STORAGE.read( userID, BlogObjectType.USERDATA, null, ContentCompressionState.UNCOMPRESSED );
    }


    /**
     * Creates a default manager user, stored at a new ID.
     *
     * @param _blogName the name of the blog this user belongs to
     */
    private static void makeDefaultUserFile( final String _blogName ) throws StorageException {

        // make our synthetic user...
        User user = User.create( "manager", _blogName, BCrypt.hashpw( "blog", BCrypt.gensalt() ) );
        user.addRight( MANAGER );
        user.addRight( AUTHOR );
        user.addRight( ADULT );
        user.addRight( REVIEWER );
        user.updateIfDirty();
    }


    /**
     * This method is invoked when there's a failure (for whatever reason) to read the users index file.  It will initialize the users instance by
     * iterating over <i>all</i> the files in the blog (potentially a lengthy process).  Once that's complete, it will serialize the instance back
     * to the users index file, to recreate it.
     *
     * @return the newly created users instance
     */
    private static Users initializeFromUserFiles( final BlogConfig _blogConfig ) throws StorageException {

        Timer t = new Timer();
        LOG.info( "Creating Users instance the hard way: reading all blog files" );

        // ensure that we have an ID...
        BlogID oldUsersID = BlogID.create( _blogConfig.getUsers() );
        BlogID usersID = (oldUsersID == null) ? BlogIDs.INSTANCE.getNextBlogID() : oldUsersID;

        Users result = new Users( usersID );

        // iterate over all the blog object files...
        BlogObjectIterator it = new BlogObjectIterator();
        while( it.hasNext() ) {

            BlogObjectInfo info = it.next();

            // if we don't have a user data file, just skip it...
            if( !info.file.getName().endsWith( ".user" ) )
                continue;

            // read the user data in and instantiate it...
            User user;
            try {
                user = (User) BlogServer.STORAGE.read( info.id, BlogObjectType.USERDATA, null, ContentCompressionState.UNCOMPRESSED );
            }
            catch( StorageException e ) {
                LOG.error( "Can't read user: ID " + info.id );
                continue;
            }

            // if this user doesn't belong to this blog, skip it...
            if( !user.getBlog().equals( _blogConfig.getDomain() ))
                continue;

            // index this user...
            result.indexUser( info.id, user );
        }

        // now write the users out to disk...
        if( oldUsersID == null )
            result = (Users) BlogServer.STORAGE.create( result );
        else
            result = result.update();

        _blogConfig.setUsers( result.getBlogID().getID() );
        _blogConfig.serialize();

        t.mark();
        LOG.info( msg( "Completed creating Users instance by reading file system in {0}", t.toString() ) );

        return result;
    }


    public Users update() throws StorageException {
        return (Users) BlogServer.STORAGE.update( this );
    }


    private static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuild( gsonBuilder );
        gsonBuilder.registerTypeAdapter( Users.class, new Deserializer() );
        gsonBuilder.registerTypeAdapter( Users.class, new Serializer()   );
        return gsonBuilder.create();
    }


    public String toJSON() throws StorageException {
        try {
            return gson().toJson( this, getClass() );
        }
        catch( Exception e ) {
            throw new StorageException( "Problem serializing Users to JSON", e );
        }
    }


    public static Users fromJSON( final String _json ) throws StorageException {
        try {
            return gson().fromJson( _json, Users.class );
        }
        catch( JsonSyntaxException e ) {
            throw new StorageException( "Problem deserializing Users from JSON", e );
        }
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
     * {
     *    "type":"USERINDEX",
     *    "blogID":"AAAAAAAAAA",
     *    "accessRequirements":null,
     *    "users":[
     *              ["AAAAAAABCD", "HamburgerHelper", "LuRdWoHieDkUsoX-_iZw"],
     *              ["AAAAAABu_Q", "bozo@circus.com", null]
     *            ]
     * }
     */


    private static class Deserializer implements JsonDeserializer<Users> {

        @Override
        public Users deserialize( final JsonElement _jsonElement,
                                  final Type _type,
                                  final JsonDeserializationContext _jsonDeserializationContext ) throws JsonParseException {

            Users result = new Users();

            if( !_jsonElement.isJsonObject() )
                throw new JsonParseException( "Expected object start" );

            JsonObject object = _jsonElement.getAsJsonObject();

            // deserialize our base object...
            result.deserialize( object );

            // iterate over all the users in the JSON file...
            JsonElement usersElement = object.get( "users" );

            if( !usersElement.isJsonArray() )
                throw new JsonParseException( "Expected array start" );

            JsonArray usersArray = usersElement.getAsJsonArray();
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


    public static class Serializer implements JsonSerializer<Users> {


        @Override
        public JsonElement serialize( final Users _users, final Type _type,
                                      final JsonSerializationContext _context ) {

            JsonObject object = new JsonObject();
            _users.serialize( object );

            JsonArray users = new JsonArray();

            // iterate over all the entries in the reverse index, as they have all the data we need...
            for( Map.Entry<byte[],Keys> entry : _users.reverse.entrySet() ) {

                JsonArray item = new JsonArray();
                item.add( fromUTF8( entry.getKey() ) );
                item.add( fromUTF8( entry.getValue().username ) );
                item.add( fromUTF8( entry.getValue().cookieValue ) );
                users.add( item );
            }

            object.add( "users", users );

            return object;
        }
    }
}
