package com.slightlyloony.blog.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.slightlyloony.blog.BlogServer;
import com.slightlyloony.blog.objects.BlogIDs;
import com.slightlyloony.blog.objects.BlogObjectObject;
import com.slightlyloony.blog.objects.BlogObjectType;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.security.BlogUserRights;
import com.slightlyloony.blog.storage.StorageException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a blog user.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class User extends BlogObjectObject {

    private static final Logger LOG = LogManager.getLogger();

    private String username;
    private String passwordHashedAndSalted;
    private String blog;
    private Instant created;

    private String cookie;
    private BlogUserRights rights;
    private boolean disabled;
    private String disabledReason;

    private Instant emailLastVerified;
    private Instant lastVisited;
    private int numberOfVisits;

    private String handle;
    private String firstName;
    private String lastName;
    private String email;

    private boolean nameIsPublic;
    private boolean emailIsPublic;

    private int birthYear;
    private String bio;
    private String motto;
    private Gender gender;
    private String image;

    private boolean dirty;


    public User( final String _username, final String _blog, final String _passwordHashedAndSalted ) {
        super( BlogIDs.INSTANCE.getNextBlogID(), BlogObjectType.USERDATA, null );

        username = _username;
        blog = _blog;
        passwordHashedAndSalted = _passwordHashedAndSalted;
        rights = new BlogUserRights();
        dirty = true;
    }


    protected User() {
        super();
        // naught to do; for use by deserializers and immutable users only...
    }


    /**
     * Creates a new instance of this class with the given parameters, and persists it.
     *
     * @param _username the new user's username
     * @param _blog the blog that the new user belongs to
     * @param _passwordHashedAndSalted the new user's hashed and salted password
     * @return the newly created user object
     * @throws StorageException on any problem
     */
    public static  User create( final String _username, final String _blog, final String _passwordHashedAndSalted ) throws StorageException {

        User result = new User( _username, _blog, _passwordHashedAndSalted );
        BlogServer.STORAGE.create( result );
        result.dirty = false;
        return result;
    }


    /**
     * Creates a new instance of this class by deserializing the given JSON data.
     *
     * @param _json the JSON representation of the user object to instantiate
     * @return the new user object
     * @throws StorageException on any problem
     */
    public static User fromJSON( final String _json ) throws StorageException {
        try {
            return gson().fromJson( _json, User.class );
        }
        catch( JsonParseException e ) {
            throw new StorageException( "Problem deserializing User from JSON", e );
        }
    }


    private static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuild( gsonBuilder );
        gsonBuilder.registerTypeAdapter( BlogUserRights.class, new BlogUserRights.Serializer()   );
        gsonBuilder.registerTypeAdapter( BlogUserRights.class, new BlogUserRights.Deserializer() );
        return gsonBuilder.create();
    }


    /**
     * Returns true if the hash of the given password matches the hashed and salted password stored in this instance.
     *
     * @param _password the password to check (in plaintext)
     * @return true if the given password matches the stored password
     */
    public boolean passwordOK( final String _password ) {
        return BCrypt.checkpw( _password, passwordHashedAndSalted );
    }


    @Override
    public int size() {

        int result = baseSize();

        // the strings...
        result += strLen( username );
        result += strLen( passwordHashedAndSalted );
        result += strLen( blog );
        result += strLen( cookie );
        result += strLen( disabledReason );
        result += strLen( handle );
        result += strLen( firstName );
        result += strLen( lastName );
        result += strLen( email );
        result += strLen( bio );
        result += strLen( motto );
        result += strLen( image );

        // the other primitives (we assume 8 bytes each)...
        result += 8 * 6;

        // the Instants (16 bytes plus a pointer)...
        result += 3 * (16 + 8);

        // the image IntegerDatum...
        result += 36;

        // the user rights...
        result += rights.size();

        return result;
    }


    private int strLen( final String _str ) {
        return (_str == null) ? 0 : 2 * _str.length();
    }


    public boolean isDisabled() {
        return disabled;
    }


    public void setDisabled( final boolean _disabled ) {

        if( checkDirty( disabled, _disabled ) )
            disabled = _disabled;
    }


    public String getDisabledReason() {
        return disabledReason;
    }


    public void setDisabledReason( final String _disabledReason ) {

        if( checkDirty( disabledReason, _disabledReason ) )
            disabledReason = _disabledReason;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername( final String _username ) {

        if( checkDirty( username, _username ) )
            username = _username;
    }


    public String getCookie() {
        return cookie;
    }


    public void setCookie( final String _cookie ) {

        if( checkDirty( cookie, _cookie ) )
            cookie = _cookie;
    }


    public BlogUserRights getRights() {
        return rights;
    }


    public void setRights( final BlogUserRights _rights ) {

        if( checkDirty( rights, _rights ) )
            rights = _rights;
    }


    public Instant getEmailLastVerified() {
        return emailLastVerified;
    }


    public void setEmailLastVerified( final Instant _emailLastVerified ) {

        if( checkDirty( emailLastVerified, _emailLastVerified ) )
            emailLastVerified = _emailLastVerified;
    }


    public Instant getLastVisited() {
        return lastVisited;
    }


    public void setLastVisited( final Instant _lastVisited ) {

        if( checkDirty( lastVisited, _lastVisited ) )
            lastVisited = _lastVisited;
    }


    public int getNumberOfVisits() {
        return numberOfVisits;
    }


    public void setNumberOfVisits( final int _numberOfVisits ) {

        if( checkDirty( numberOfVisits, _numberOfVisits ) )
            numberOfVisits = _numberOfVisits;
    }


    public String getHandle() {
        return handle;
    }


    public void setHandle( final String _handle ) {

        if( checkDirty( handle, _handle ) )
            handle = _handle;
    }


    public String getFirstName() {
        return firstName;
    }


    public void setFirstName( final String _firstName ) {

        if( checkDirty( firstName, _firstName ) )
            firstName = _firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public void setLastName( final String _lastName ) {

        if( checkDirty( lastName, _lastName ) )
            lastName = _lastName;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail( final String _email ) {

        if( checkDirty( email, _email ) )
            email = _email;
    }


    public boolean isNameIsPublic() {
        return nameIsPublic;
    }


    public void setNameIsPublic( final boolean _nameIsPublic ) {

        if( checkDirty( nameIsPublic, _nameIsPublic ) )
            nameIsPublic = _nameIsPublic;
    }


    public boolean isEmailIsPublic() {
        return emailIsPublic;
    }


    public void setEmailIsPublic( final boolean _emailIsPublic ) {

        if( checkDirty( emailIsPublic, _emailIsPublic ) )
            emailIsPublic = _emailIsPublic;
    }


    public int getBirthYear() {
        return birthYear;
    }


    public void setBirthYear( final int _birthYear ) {

        if( checkDirty( birthYear, _birthYear ) )
            birthYear = _birthYear;
    }


    public String getBio() {
        return bio;
    }


    public void setBio( final String _bio ) {

        if( checkDirty( bio, _bio ) )
            bio = _bio;
    }


    public String getMotto() {
        return motto;
    }


    public void setMotto( final String _motto ) {

        if( checkDirty( motto, _motto ) )
            motto = _motto;
    }


    public Gender getGender() {
        return gender;
    }


    public void setGender( final Gender _gender ) {

        if( checkDirty( gender, _gender ) )
            gender = _gender;
    }


    public String getImage() {
        return image;
    }


    public void setImage( final String _image ) {

        if( checkDirty( image, _image ) )
            image = _image;
    }


    public String getPasswordHashedAndSalted() {
        return passwordHashedAndSalted;
    }


    public void setPasswordHashedAndSalted( final String _passwordHashedAndSalted ) {

        if( checkDirty( passwordHashedAndSalted, _passwordHashedAndSalted ) )
            passwordHashedAndSalted = _passwordHashedAndSalted;
    }


    public String getBlog() {
        return blog;
    }


    public void setBlog( final String _blog ) {

        if( checkDirty( blog, _blog ) )
            blog = _blog;
    }


    public boolean isDirty() {
        return dirty;
    }


    public Instant getCreated() {
        return created;
    }


    public void setCreated( final Instant _created ) {
        created = _created;
    }


    public void addRight( final BlogAccessRight _right ) {
        rights.add( _right );
        dirty = true;
    }


    public boolean hasRight( final BlogAccessRight _right ) {
        return rights.has( _right );
    }


    private boolean checkDirty( final Object _o1, final Object _o2 ) {

        if( !Objects.equals( _o1, _o2 ) )
            dirty = true;
        return dirty;
    }


    public void updateIfDirty() throws StorageException {

        if( !dirty )
            return;

        BlogServer.STORAGE.update( this );
        dirty = false;
    }


    public String toJSON() throws StorageException {
        try {
            return gson().toJson( this, getClass() );
        }
        catch( Exception e ) {
            throw new StorageException( "Problem serializing User to JSON", e );
        }
    }
}
