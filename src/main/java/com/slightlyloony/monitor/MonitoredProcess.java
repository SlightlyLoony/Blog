package com.slightlyloony.monitor;

import com.slightlyloony.common.ipmsgs.IPMsg;
import com.slightlyloony.common.ipmsgs.IPMsgSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.slightlyloony.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitoredProcess {

    private static final int    MAX_STDOUT_QUEUE_SIZE = 1000;  // this is a count of received blocks, not bytes or characters...
    private static final Logger LOG                   = LogManager.getLogger();

    private final MonitorConfig.Server serverConfig;
    private final String name;
    private final LinkedBlockingQueue<String> processOutput;
    private final SocketAddress ipmSocket;

    private Process process;
    private StdOutReader reader;


    public MonitoredProcess( final String _name, final MonitorConfig.Server _serverConfig ) {
        serverConfig = _serverConfig;
        name = _name;
        processOutput = new LinkedBlockingQueue<> (MAX_STDOUT_QUEUE_SIZE );
        ipmSocket = serverConfig.getSocketAddress();
    }


    public void send( final IPMsg _msg ) throws IOException {
        IPMsgSocket.INSTANCE.send( _msg, ipmSocket );
    }


    public boolean isAlive() {
        return (process != null) && (process.isAlive());
    }


    public void runProcess() throws IOException {

        // refuse to run if we're already running...
        if( isAlive() )
            throw new IllegalStateException( "Attempted to start process that was already running" );

        // make our command line as a list of elements...
        List<String> cl = new ArrayList<>();
        cl.add( MonitorInit.getConfig().getJavaPath() );
        cl.add( "-Duser.dir=\"" + serverConfig.getWorkingDir() + "\"" );
        cl.add( "-Dfile.encoding=UTF-8" );
        cl.add( "-jar" );
        cl.add( serverConfig.getJarFile() );

        // make our process builder...
        ProcessBuilder pb = new ProcessBuilder( cl );
        pb.redirectErrorStream( true );  // merge error stream into stdout...
        pb.directory( new File( serverConfig.getWorkingDir() ) );  // this may be redundant with -Duser.dir; not certain...

        // now we actually do it...
        process = pb.start();
        reader = new StdOutReader( process.getInputStream() );
        reader.start();
    }


    public void stopProcess() {
        if( isAlive() ) {
            reader.interrupt();
            process.destroyForcibly();
            try {
                process.waitFor();
                processOutput.clear();
                LOG.info( msg( "Process \"{0}\" terminated", name ) );
            }
            catch( InterruptedException e ) {
                LOG.error( msg( "Interrupted while waiting for process \"{0}\" to terminate", name ), e );
            }
        }
    }


    /**
     * Returns as much of the monitored process' stdout as is available at the time this method is invoked.  The returned string may be
     * zero length, indicating no stdout is available.
     *
     * @return a string containing all available stdout from the monitored process
     */
    public String getStdOut() {
        StringBuilder sb = new StringBuilder();
        String s;
        while( (s = processOutput.poll() ) != null )
            sb.append( s );
        return sb.toString();
    }


    private class StdOutReader extends Thread {

        private final InputStream stdOut;


        private StdOutReader( final InputStream _stdOut ) {
            super( name + "-sor" );
            setDaemon( true );
            stdOut = _stdOut;
        }

        @Override
        public void run() {

            try {
                byte[] b = new byte[10000];
                while( !interrupted() ) {

                    // read some bytes...
                    int count = stdOut.read( b );

                    // if we got an EOF, we've got a problem or we're shutting down...
                    if( count < 0 )
                        break;

                    // convert what we read to a string and stuff it into our queue...
                    // note this potentially a problem, as there is a remote possibility that a multi-byte code was at the end of this buffer...
                    String s = new String( b, 0, count, "UTF-8" );
                    processOutput.add( s );
                }
            }

            catch( final IllegalStateException e ) {
                LOG.error( msg( "Size of stdout queue exceeded for monitored process \"{0}\"", name ) );
            }
            catch( final ClosedByInterruptException e ) {
                LOG.error( msg( "Stdout reader for monitored process \"{0}\" closed by interrupt", name ) );
            }
            catch( final Exception e ) {
                LOG.error( msg( "Error while reading stdout of monitored process \"{0}\"", name ), e );
            }

            // if we get here, we had some kind of problem and it's time to kill the process...
            if( process.isAlive() ) {
                process.destroyForcibly();
                try {
                    process.waitFor();
                    LOG.info( msg( "Process \"{0}\" terminated because of error while reading stdout", name ) );
                }
                catch( InterruptedException e1 ) {
                    LOG.error( msg( "Interrupted while waiting for process \"{0}\" to terminate", name ), e1 );
                }
            }
        }
    }
}
