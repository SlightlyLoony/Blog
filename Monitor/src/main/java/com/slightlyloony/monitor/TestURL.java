package com.slightlyloony.monitor;

import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TestURL {

    private static final Logger LOG = LogManager.getLogger();

    private final String urlString;

    private String location;
    private int responseCode;
    private String content;


    public TestURL( final String _url ) {
        urlString = _url;
    }


    public boolean test() {

        try {
            URL url = new URL( urlString );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setAllowUserInteraction( false );
            conn.setConnectTimeout( 1000 );
            conn.setReadTimeout( 1000 );
            conn.setInstanceFollowRedirects( false );
            conn.setUseCaches( false );
            conn.connect();

            responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
            StringBuilder sb = new StringBuilder();
            for(String s; (s = br.readLine()) != null; ) {
                sb.append( s );
                sb.append( "\n" );
            }
            content = sb.toString();
            location = conn.getHeaderField( "Location" );
        }
        catch( IOException e ) {
            LOG.error( LU.msg( "Problem retrieving test URL {0}: {1}", urlString, e.getMessage() ) );
            return false;
        }
        LOG.debug( LU.msg( "Successfully retrieved test URL {0}", urlString ) );
        return true;
    }


    public String getLocation() {
        return location;
    }


    public int getResponseCode() {
        return responseCode;
    }


    public String getContent() {
        return content;
    }
}
