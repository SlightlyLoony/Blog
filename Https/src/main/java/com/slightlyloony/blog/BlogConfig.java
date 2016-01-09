package com.slightlyloony.blog;

import java.net.InetSocketAddress;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogConfig {

    private Server monitor;
    private Server http;
    private Server https;
    private VirtualServer[] virtualServers;
    private String keystore;
    private String keystorePassword;  // this implementation assumes the same password is used for both the keystore and the certificate...


    public Server getMonitor() {
        return monitor;
    }


    public VirtualServer[] getVirtualServers() {
        return virtualServers;
    }


    public Server getHttp() {
        return http;
    }


    public Server getHttps() {
        return https;
    }


    public String getKeystore() {
        return keystore;
    }


    public String getKeystorePassword() {
        return keystorePassword;
    }


    public static class VirtualServer {

        private int port;
        private String domain;
        private String alias;


        public int getPort() {
            return port;
        }


        public String getDomain() {
            return domain;
        }


        public String getAlias() {
            return alias;
        }
    }


    public static class Server {

        private String ip;
        private int port;
        private String dir;
        private String jar;


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


        public InetSocketAddress getSocketAddress() {
            return new InetSocketAddress( ip, port );
        }
    }
}
