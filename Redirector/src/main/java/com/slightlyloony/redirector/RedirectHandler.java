package com.slightlyloony.redirector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class RedirectHandler extends AbstractHandler {

    private static final Logger LOG = LogManager.getLogger();


    public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException {

        // figure out the URL we want to redirect to...
        String host = request.getHeader( "Host" );
        int colon = host.lastIndexOf( ":" );
        if( colon > 0 )
            host = host.substring( 0, host.lastIndexOf( ":" ) );
        String path = request.getPathInfo();
        String queryString = request.getQueryString();
        int httpsPort = RedirectorInit.getConfig().getHttpsPort();
        String portString = (httpsPort == 443) ? "" : ":" + httpsPort;
        String url = "https://" + host + portString + path;
        if( (queryString != null) && (queryString.length() > 0))
            url += "?" + queryString;
        LOG.info( "Redirecting to URL: " + url );


        response.setContentType( "text/html; charset=utf-8" );
        response.setHeader( "Location", url );
        response.setStatus( HttpServletResponse.SC_MOVED_PERMANENTLY );

        PrintWriter out = response.getWriter();

        out.println("<h1>Redirected</h1>");

        baseRequest.setHandled(true);
    }
}