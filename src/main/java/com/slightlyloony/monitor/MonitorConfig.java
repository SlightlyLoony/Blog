package com.slightlyloony.monitor;

import com.slightlyloony.mail.MailCredential;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitorConfig {

    private int    monitorIPport;
    private String monitorIPaddr;
    private int    httpsIPport;
    private String httpsIPaddr;
    private int    httpIPport;
    private String httpIPaddr;
    private int    mailPortalFetchIntervalSeconds;
    private int    executorThreads;
    private String mailPortalAuthorizedUser;
    private String monitorEmailUser;
    private String monitorEmailPassword;


    public int getMailPortalFetchIntervalSeconds() {
        return mailPortalFetchIntervalSeconds;
    }


    public String getMailPortalAuthorizedUser() {
        return mailPortalAuthorizedUser;
    }


    public String getMonitorEmailUser() {
        return monitorEmailUser;
    }


    public String getMonitorEmailPassword() {
        return monitorEmailPassword;
    }


    public int getExecutorThreads() {
        return executorThreads;
    }


    public int getMonitorIPport() {
        return monitorIPport;
    }


    public String getMonitorIPaddr() {
        return monitorIPaddr;
    }


    public int getHttpsIPport() {
        return httpsIPport;
    }


    public String getHttpsIPaddr() {
        return httpsIPaddr;
    }


    public int getHttpIPport() {
        return httpIPport;
    }


    public String getHttpIPaddr() {
        return httpIPaddr;
    }


    public SocketAddress getMonitorIPSocketAddress() {
        return new InetSocketAddress( monitorIPaddr, monitorIPport );
    }


    public SocketAddress getHttpsIPSocketAddress() {
        return new InetSocketAddress( httpsIPaddr, httpsIPport );
    }


    public SocketAddress getHttpIPSocketAddress() {
        return new InetSocketAddress( httpIPaddr, httpIPport );
    }


    public MailCredential getMailCredential() {
        return new MailCredential( monitorEmailUser, monitorEmailPassword );
    }
}
