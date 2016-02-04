package com.slightlyloony.blog.users;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.slightlyloony.blog.objects.BlogID;
import com.slightlyloony.blog.objects.BlogObjectType;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;
import com.slightlyloony.blog.security.BlogUserRights;
import com.slightlyloony.blog.storage.StorageException;

import java.time.Instant;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ImmutableUser extends User {

    private final User user;


    public ImmutableUser( final User _user ) {
        user = _user;
    }


    /**
     * Creates a new instance of this class with the given parameters, and persists it.
     *
     * @param _username the new user's username
     * @param _blog the blog that the new user belongs to
     * @param _passwordHashedAndSalted the new user's hashed and salted password
     * @return the newly created user object
     * @throws StorageException on any problem
     * @deprecated This operation is not supported on an {@link ImmutableUser} object; it is guaranteed to throw an
     *             {@link UnsupportedOperationException}.
     */
    @Deprecated
    public static User create( final String _username, final String _blog, final String _passwordHashedAndSalted ) throws StorageException {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    /**
     * Creates a new instance of this class by deserializing the given JSON data.
     *
     * @param _json the JSON representation of the user object to instantiate
     * @return the new user object
     * @throws StorageException on any problem
     * @deprecated This operation is not supported on an {@link ImmutableUser} object; it is guaranteed to throw an
     *             {@link UnsupportedOperationException}.
     */
    @Deprecated
    public static User fromJSON( final String _json ) throws StorageException {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    /**
     * Returns true if the hash of the given password matches the hashed and salted password stored in this instance.
     *
     * @param _password the password to check (in plaintext)
     * @return true if the given password matches the stored password
     */
    @Override
    public boolean passwordOK( final String _password ) {
        return user.passwordOK( _password );
    }


    @Override
    public int size() {
        return user.size();
    }


    @Override
    public boolean isDisabled() {
        return user.isDisabled();
    }


    @Override
    @Deprecated
    public void setDisabled( final boolean _disabled ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getDisabledReason() {
        return user.getDisabledReason();
    }


    @Override
    @Deprecated
    public void setDisabledReason( final String _disabledReason ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getUsername() {
        return user.getUsername();
    }


    @Override
    @Deprecated
    public void setUsername( final String _username ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getCookie() {
        return user.getCookie();
    }


    @Override
    @Deprecated
    public void setCookie( final String _cookie ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public BlogUserRights getRights() {
        return user.getRights();
    }


    @Override
    @Deprecated
    public void setRights( final BlogUserRights _rights ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public Instant getEmailLastVerified() {
        return user.getEmailLastVerified();
    }


    @Override
    @Deprecated
    public void setEmailLastVerified( final Instant _emailLastVerified ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public Instant getLastVisited() {
        return user.getLastVisited();
    }


    @Override
    @Deprecated
    public void setLastVisited( final Instant _lastVisited ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public int getNumberOfVisits() {
        return user.getNumberOfVisits();
    }


    @Override
    @Deprecated
    public void setNumberOfVisits( final int _numberOfVisits ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getHandle() {
        return user.getHandle();
    }


    @Override
    @Deprecated
    public void setHandle( final String _handle ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getFirstName() {
        return user.getFirstName();
    }


    @Override
    @Deprecated
    public void setFirstName( final String _firstName ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getLastName() {
        return user.getLastName();
    }


    @Override
    @Deprecated
    public void setLastName( final String _lastName ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getEmail() {
        return user.getEmail();
    }


    @Override
    @Deprecated
    public void setEmail( final String _email ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public boolean isNameIsPublic() {
        return user.isNameIsPublic();
    }


    @Override
    @Deprecated
    public void setNameIsPublic( final boolean _nameIsPublic ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public boolean isEmailIsPublic() {
        return user.isEmailIsPublic();
    }


    @Override
    @Deprecated
    public void setEmailIsPublic( final boolean _emailIsPublic ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public int getBirthYear() {
        return user.getBirthYear();
    }


    @Override
    @Deprecated
    public void setBirthYear( final int _birthYear ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getBio() {
        return user.getBio();
    }


    @Override
    @Deprecated
    public void setBio( final String _bio ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getMotto() {
        return user.getMotto();
    }


    @Override
    @Deprecated
    public void setMotto( final String _motto ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public Gender getGender() {
        return user.getGender();
    }


    @Override
    @Deprecated
    public void setGender( final Gender _gender ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getImage() {
        return user.getImage();
    }


    @Override
    @Deprecated
    public void setImage( final String _image ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getPasswordHashedAndSalted() {
        return user.getPasswordHashedAndSalted();
    }


    @Override
    @Deprecated
    public void setPasswordHashedAndSalted( final String _passwordHashedAndSalted ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public String getBlog() {
        return user.getBlog();
    }


    @Override
    @Deprecated
    public void setBlog( final String _blog ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    @Deprecated
    public boolean isDirty() {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public Instant getCreated() {
        return user.getCreated();
    }


    @Override
    @Deprecated
    public void setCreated( final Instant _created ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    @Deprecated
    public void addRight( final BlogAccessRight _right ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public boolean hasRight( final BlogAccessRight _right ) {
        return user.hasRight( _right );
    }


    @Override
    @Deprecated
    public void updateIfDirty() throws StorageException {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    @Deprecated
    public String toJSON() throws StorageException {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    /**
     * Resolves this instance into cacheable form, possibly compressed (if content).
     *
     * @param _mayCompress true if this instance may be compressed
     * @deprecated This operation is not supported on an {@link ImmutableUser} object; it is guaranteed to throw an
     *             {@link UnsupportedOperationException}.
     */
    @Override
    @Deprecated
    public void makeReadyForCache( final boolean _mayCompress ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    /**
     * The approximate memory requirements for this instance, in bytes.
     *
     * @return the approximate number of bytes this instance occupies in memory
     * @deprecated This operation is not supported on an {@link ImmutableUser} object; it is guaranteed to throw an
     *             {@link UnsupportedOperationException}.
     */
    @Override
    @Deprecated
    public int baseSize() {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    public BlogID getBlogID() {
        return user.getBlogID();
    }


    @Override
    public BlogObjectType getType() {
        return user.getType();
    }


    @Override
    public BlogObjectAccessRequirements getAccessRequirements() {
        return user.getAccessRequirements();
    }


    @Override
    @Deprecated
    public void serialize( final JsonObject _object ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Override
    @Deprecated
    public void deserialize( final JsonObject _object ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }


    @Deprecated
    public static void gsonBuild( final GsonBuilder _builder ) {
        throw new UnsupportedOperationException( "Operation not allowed on ImmutableUser" );
    }
}
