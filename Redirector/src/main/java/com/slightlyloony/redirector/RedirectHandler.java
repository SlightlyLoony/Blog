package com.slightlyloony.redirector;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Map;

public class RedirectHandler extends AbstractHandler {

    private static final Logger LOG = LogManager.getLogger();
    private static final String MIN_HTML =
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html lang=\"en\"><head>" +
                    "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"><title>{0}</title></head>" +
                    "<body>{1}</body></html>";

    private Map<String, Integer> domainToPort;


    public RedirectHandler() {

        domainToPort = Maps.newHashMap();
        for( RedirectorConfig.VirtualServer virtualServer : RedirectorInit.getConfig().getVirtualServers() ) {
            domainToPort.put( virtualServer.getDomain(), virtualServer.getPort() );
        }
    }


    public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException {

        // figure out the URL we want to redirect to...
        String host = request.getHeader( "Host" );
        int colon = host.lastIndexOf( ":" );
        if( colon > 0 )
            host = host.substring( 0, host.lastIndexOf( ":" ) );
        String[] parts = host.split( "\\." );
        String domain = parts[parts.length - 2] + "." + parts[parts.length - 1];
        String path = request.getPathInfo();
        String queryString = request.getQueryString();

        // if this request is coming from some domain we don't know, let the user know they messed up...
        if( !domainToPort.containsKey( domain ) ) {

            LOG.info( "Request from unknown domain: " + domain );

            response.setContentType( "text/html; charset=utf-8" );
            response.setStatus( HttpServletResponse.SC_OK );

            // generate a nice little message for our oddball...
            StringBuilder sb = new StringBuilder();
            sb.append( "<h1>Oopsie!  Were you trying to go to one of these sites?</h1>" );
            for( RedirectorConfig.VirtualServer virtualServer : RedirectorInit.getConfig().getVirtualServers() ) {
                sb.append( "<p><a href=\"https://" );
                sb.append( virtualServer.getDomain() );
                sb.append( ":" );
                sb.append( virtualServer.getPort() );
                sb.append( "\">" );
                sb.append( virtualServer.getName() );
                sb.append( "</a></p>" );
            }

            PrintWriter out = response.getWriter();
            out.println( MessageFormat.format( MIN_HTML, "Oopsie!", sb.toString() ) );
        }
        else {
            int httpsPort = domainToPort.get( domain );
            String portString = (httpsPort == 443) ? "" : ":" + httpsPort;
            String url = "https://" + host + portString + path;
            if( (queryString != null) && (queryString.length() > 0) )
                url += "?" + queryString;
            LOG.info( "Redirecting to URL: " + url );

            response.setContentType( "text/html; charset=utf-8" );
            response.setHeader( "Location", url );
            response.setStatus( HttpServletResponse.SC_MOVED_PERMANENTLY );

            PrintWriter out = response.getWriter();
            out.println( MessageFormat.format( MIN_HTML, "Redirecting...", "<h1>" + url + "</h1>" ));
        }

        baseRequest.setHandled(true);
    }
}
