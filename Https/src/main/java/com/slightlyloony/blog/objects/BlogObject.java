package com.slightlyloony.blog.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.security.BlogObjectAccessRequirements;

/**
 * Represents an object stored on the blog, which could be metadata, static (including template generated) content, or content generated by code.
 * The class includes provision for either storing the encoded byte form of the object, or as an input stream with known length.  When an instance
 * of this class resides in a cache, it is <i>always</i> in encoded byte form, compressed if possible, to minimize the memory consumed.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class BlogObject {

    protected BlogID blogID;
    protected BlogObjectType type;
    protected BlogObjectAccessRequirements accessRequirements;


    protected BlogObject( final BlogID _id, final BlogObjectType _type, final BlogObjectAccessRequirements _accessRequirements ) {

        if( (_type == null) || (_id == null) )
            throw new HandlerIllegalArgumentException( "Missing required argument _type or _id" );

        type = _type;
        blogID = _id;
        accessRequirements = _accessRequirements;
    }


    /**
     * This constructor should be used by deserializers <i>only</i>.
     */
    protected BlogObject() {
        type = null;
        blogID = null;
        accessRequirements = null;
    }


    /**
     * The approximate memory requirements for this instance, in bytes.
     *
     * @return the approximate number of bytes this instance occupies in memory
     */
    public int size() {
        return 36 + 8 + 8;
    }


    /**
     * Resolves this instance into cacheable form, possibly compressed (if content).
     *
     * @param _mayCompress true if this instance may be compressed
     */
    public abstract void makeReadyForCache( final boolean _mayCompress );


    public BlogID getBlogID() {
        return blogID;
    }


    public BlogObjectType getType() {
        return type;
    }


    public BlogObjectAccessRequirements getAccessRequirements() {
        return accessRequirements;
    }


    protected void serialize( final JsonObject _object ) {
        Gson gson = gson();
        _object.add( "type", gson.toJsonTree( type ) );
        _object.add( "blogID", gson.toJsonTree( blogID ));
        _object.add( "accessRequirements", gson.toJsonTree( accessRequirements ) );
    }


    protected void deserialize( final JsonObject _object ) {
        Gson gson = gson();
        if( _object.has( "type" ))
            type = gson.fromJson( _object.get( "type" ), BlogObjectType.class );
        if( _object.has( "blogID" ))
            blogID = gson.fromJson( _object.get( "blogID" ), BlogID.class );
        if( _object.has( "accessRequirements" ))
            accessRequirements = gson.fromJson( _object.get( "accessRequirements" ), BlogObjectAccessRequirements.class );
    }


    private Gson gson() {
        GsonBuilder builder = new GsonBuilder();
        gsonBuild( builder );
        return builder.create();
    }


    protected static void gsonBuild( final GsonBuilder _builder ) {
        _builder.registerTypeAdapter( BlogObjectAccessRequirements.class, new BlogObjectAccessRequirements.Serializer()   );
        _builder.registerTypeAdapter( BlogObjectAccessRequirements.class, new BlogObjectAccessRequirements.Deserializer() );
        _builder.registerTypeAdapter( BlogID.class,                       new BlogID.Serializer()                         );
        _builder.registerTypeAdapter( BlogID.class,                       new BlogID.Deserializer()                       );
    }
}
