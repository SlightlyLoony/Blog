package com.slightlyloony.blog.config;

import com.google.gson.Gson;
import com.slightlyloony.blog.ServerInit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;

import static com.slightlyloony.blog.util.S.toUTF8;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogConfig {

    private static final Logger LOG = LogManager.getLogger();

    private static final int DEFAULT_HTTPS_PORT = 443;

    Map<String,String> mappings;
    private int port;
    private String domain;
    private String certAlias;
    private String users;  // blog IntegerDatum of users index file...


    public static BlogConfig readConfig( final String _name ) {

        try {
            File blogConfigFile = new File( new File( new File( ServerInit.getConfig().getContentRoot() ), _name ), "blog.json");
            return new Gson().fromJson( new FileReader( blogConfigFile ), BlogConfig.class );
        }
        catch( FileNotFoundException e ) {
            LOG.fatal( "Problem reading blog configuration", e );
            return null;
        }
    }


    public String map( final String _path ) {
        return mappings.getOrDefault( _path, _path );
    }


    public int getPort() {
        return port;
    }


    public String getDomain() {
        return domain;
    }


    public String getCertAlias() {
        return certAlias;
    }


    public String getUsers() {
        return users;
    }


    public void setUsers( final String _users ) {
        users = _users;
    }


    public void serialize() {

        String json = new Gson().toJson( this, BlogConfig.class );
        File blogConfigFile = new File( new File( new File( ServerInit.getConfig().getContentRoot() ), domain ), "blog.json");
        ;
        try( FileOutputStream fos = new FileOutputStream( blogConfigFile ); ) {
            fos.write( toUTF8( json ) );
        }
        catch( IOException e ) {
            LOG.fatal( "Problem writing blog configuration", e );
        }
    }


    public String getHost() {
        return "www." + (( port == DEFAULT_HTTPS_PORT ) ? domain : domain + ":" + port);
    }


    public boolean isMinimallyValid() {

        return !((port != 443) && (port < 1024)) && (domain != null)
                && domain.matches( "[a-zA-Z0-9][a-zA-Z0-9\\-]+[a-zA-Z0-9]\\.[a-zA-Z0-9][a-zA-Z0-9\\-]+[a-zA-Z0-9]" );
    }
}
