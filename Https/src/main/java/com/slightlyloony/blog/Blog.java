package com.slightlyloony.blog;

import com.slightlyloony.blog.config.BlogConfig;
import com.slightlyloony.blog.users.Users;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Blog {

    private static final Logger LOG = LogManager.getLogger();

    private final String name;
    private final String host;
    private final BlogConfig config;
    private final Users users;


    private Blog( final String _name, final String _host, final BlogConfig _config, final Users _users ) {
        name = _name;
        host = _host;
        config = _config;
        users = _users;
    }


    public String getName() {
        return name;
    }


    public String getHost() {
        return host;
    }


    public BlogConfig getConfig() {
        return config;
    }


    public Users getUsers() {
        return users;
    }


    public static Blog create( final String _name ) {

        // if some idiot didn't give us a name, just leave with failure...
        if( _name == null ) {
            LOG.error( "Invalid blog name: null" );
            return null;
        }

        // see if we can read the configuration...
        BlogConfig config = BlogConfig.readConfig( _name );
        if( (config == null) || !config.isMinimallyValid() ) {
            LOG.error( "Invalid blog configuration: " + _name );
            return null;
        }

        // now read our users...
        Users users = Users.create( config );
        if( users == null )
            return null;

        // looks like we might actually be able to make a blog...
        return new Blog( _name, config.getHost(), config, users );
    }
}
