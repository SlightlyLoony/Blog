package com.slightlyloony.monitor;

import com.slightlyloony.common.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

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

            HttpURLConnection conn;
            URL url = new URL( urlString );

            if( urlString.startsWith( "https:" )) {
                // this stuff allows me to use self-signed certificates...
                KeyStore ks = KeyStore.getInstance( "JKS" );
                InputStream readStream = new FileInputStream( MonitorInit.getConfig().getKeystore() );
                ks.load( readStream, MonitorInit.getConfig().getKeystorePassword().toCharArray() );
                readStream.close();

                TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
                tmf.init( ks );
                SSLContext ctx = SSLContext.getInstance( "TLS" );
                ctx.init( null, tmf.getTrustManagers(), null );
                SSLSocketFactory sslFactory = ctx.getSocketFactory();

                conn = (HttpsURLConnection)url.openConnection();
                ((HttpsURLConnection)conn).setSSLSocketFactory( sslFactory );
            }

            else {
                conn = (HttpURLConnection) url.openConnection();
            }
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
        catch( Exception e ) {
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
