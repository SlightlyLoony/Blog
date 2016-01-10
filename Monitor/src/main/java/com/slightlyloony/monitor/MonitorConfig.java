package com.slightlyloony.monitor;

import com.slightlyloony.common.mail.MailCredential;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitorConfig {

    private Server monitor;
    private Server http;
    private Server https;
    private int    mailPortalFetchIntervalSeconds;
    private String mailPortalAuthorizedUser;
    private String monitorEmailUser;
    private String monitorEmailPassword;
    private String javaPath;
    private String keystore;
    private String keystorePassword;


    public String getKeystore() {
        return keystore;
    }


    public String getKeystorePassword() {
        return keystorePassword;
    }


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


    public Server getMonitor() {
        return monitor;
    }


    public Server getHttp() {
        return http;
    }


    public Server getHttps() {
        return https;
    }


    public String getJavaPath() {
        return javaPath;
    }


    public MailCredential getMailCredential() {
        return new MailCredential( monitorEmailUser, monitorEmailPassword );
    }

    public static class Server {

        private String ip;
        private int port;
        private String dir;
        private String jar;
        private String test;


        public String getIp() {
            return ip;
        }


        public int getPort() {
            return port;
        }


        public String getWorkingDir() {
            return dir;
        }


        public String getJarFile() {
            return jar;
        }


        public SocketAddress getSocketAddress() {
            return new InetSocketAddress( ip, port );
        }


        public String getTest() {
            return test;
        }
    }
}
