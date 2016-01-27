package com.slightlyloony.blog.templates.sources.data;

import com.slightlyloony.blog.templates.sources.Source;
import com.slightlyloony.blog.users.User;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Authorizor {

    boolean isAuthorized( final User _user, final Source _source );
}
