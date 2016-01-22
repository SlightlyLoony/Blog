package com.slightlyloony.blog.config;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ServerConfig {

    private Server monitor;
    private Server http;
    private Server https;
    private String keystore;
    private String keystorePassword;  // this implementation assumes the same password is used for both the keystore and the certificate...
    private String contentRoot;
    private Map<String,Cache> caches;
    private int maxCacheEntrySize;
    private int sessionIdleTimeout;  // session idle timeout in milliseconds...
    private String[] blogs;


    public int getMaxCacheEntrySize() {
        return maxCacheEntrySize;
    }


    public Map<String, Cache> getCaches() {
        return caches;
    }


    public Server getMonitor() {
        return monitor;
    }


    public String getContentRoot() {
        return contentRoot;
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


    public int getSessionIdleTimeout() {
        return sessionIdleTimeout;
    }


    public String[] getBlogs() {
        return blogs;
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


    public static class Cache {
        private long maxCacheSize;
        private long avgEntrySize;


        public long getMaxCacheSize() {
            return maxCacheSize;
        }


        public long getAvgEntrySize() {
            return avgEntrySize;
        }
    }
}
