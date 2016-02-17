package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.storage.StorageException;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.users.Users;

import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class UsersSource extends ListSource {


    /**
     * Creates a new instance of this class.  See the class comments for more details.
     *
     * @param _users the users index object
     */
    public UsersSource( final Users _users ) throws StorageException {
        super( getUsers( _users ) );
    }


    private static List<Source> getUsers( final Users _users ) throws StorageException {

        List<Source> result = Lists.newArrayList();
        for( User user : _users.getUsers() )
            result.add( new UserSource( user ) );
        return result;
    }
}
