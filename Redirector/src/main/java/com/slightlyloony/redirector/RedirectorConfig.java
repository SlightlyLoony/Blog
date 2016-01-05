package com.slightlyloony.redirector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RedirectorConfig {

    private Server monitor;
    private Server http;
    private Server https;
    private int port;
    private int maxThreads;
    private int minThreads;
    private int threadTimeoutMillis;


    public Server getMonitor() {
        return monitor;
    }


    public Server getHttp() {
        return http;
    }


    public Server getHttps() {
        return https;
    }


    public int getPort() {
        return port;
    }


    public int getMaxThreads() {
        return maxThreads;
    }


    public int getMinThreads() {
        return minThreads;
    }


    public int getThreadTimeoutMillis() {
        return threadTimeoutMillis;
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


        public SocketAddress getSocketAddress() {
            return new InetSocketAddress( ip, port );
        }
    }
}
