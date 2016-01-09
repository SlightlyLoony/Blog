package com.slightlyloony.blog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class TestHandler extends AbstractHandler {

    private static final Logger LOG = LogManager.getLogger();


    public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
            throws IOException, ServletException {

        response.setContentType( "text/html; charset=utf-8" );
        response.setStatus( HttpServletResponse.SC_OK );

        PrintWriter out = response.getWriter();
        out.println("Test");

        baseRequest.setHandled(true);
        LOG.info( "Handled " + request.getRequestURI() );
    }
}