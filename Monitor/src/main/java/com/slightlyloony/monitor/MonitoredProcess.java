package com.slightlyloony.monitor;

import com.slightlyloony.common.ipmsgs.IPMsgParticipant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitoredProcess {

    private static final int    MAX_STDOUT_QUEUE_SIZE = 1000;  // this is a count of received blocks, not bytes or characters...
    private static final Logger LOG                   = LogManager.getLogger();

    private final MonitorConfig.Server serverConfig;
    private final IPMsgParticipant server;
    private final LinkedBlockingQueue<String> processOutput;

    private Process process;
    private StdOutReader reader;


    public MonitoredProcess( final IPMsgParticipant _server, final MonitorConfig.Server _serverConfig ) {
        serverConfig = _serverConfig;
        server = _server;
        processOutput = new LinkedBlockingQueue<> (MAX_STDOUT_QUEUE_SIZE );
    }


    public boolean isAlive() {
        return (process != null) && (process.isAlive());
    }


    public void waitForDead() throws InterruptedException {
        process.waitFor();
    }


    public void runProcess() throws IOException {

        // refuse to run if we're already running...
        if( isAlive() )
            throw new IllegalStateException( "Attempted to start process that was already running" );

        // make our command line as a list of elements...
        List<String> cl = new ArrayList<>();
        cl.add( MonitorInit.getConfig().getJavaPath() );
        cl.add( "-Duser.dir=\"" + serverConfig.getWorkingDir() + "\"" );
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
                LOG.info( msg( "Process \"{0}\" terminated", server ) );
            }
            catch( InterruptedException e ) {
                LOG.error( msg( "Interrupted while waiting for process \"{0}\" to terminate", server ), e );
            }
        }
    }


    /**
     * Returns the next line of stdout, or null if none available.
     *
     * @return the next line of stdout, or null if none available
     */
    public String getStdOutLine() {
        return processOutput.poll();
    }


    private class StdOutReader extends Thread {

        private BufferedReader stdOut;


        private StdOutReader( final InputStream _stdOut ) {
            super( server + "-sor" );
            setDaemon( true );
            try {
                stdOut = new BufferedReader( new InputStreamReader( _stdOut, "UTF-8" ) );
            }
            catch( UnsupportedEncodingException e ) {
                LOG.error( "Problem when establishing STDOUT reader", e );
            }
        }

        @Override
        public void run() {

            boolean stopping = false;
            try {
                while( !interrupted() ) {

                    // read a line...
                    String line = stdOut.readLine();

                    // if we got an EOF, we've got a problem or we're shutting down...
                    if( line == null )
                        break;

                    // convert what we read to a string and stuff it into our queue...
                    // note this potentially a problem, as there is a remote possibility that a multi-byte code was at the end of this buffer...
                    processOutput.add( line );

                    LOG.debug( server + " STDOUT: " + line );
                }

                // if we get here, we're stopping on purpose...
                stopping = true;
            }

            catch( final IllegalStateException e ) {
                LOG.error( msg( "Size of stdout queue exceeded for monitored process \"{0}\"", server ) );
            }
            catch( final ClosedByInterruptException e ) {
                LOG.error( msg( "Stdout reader for monitored process \"{0}\" closed by interrupt", server ) );
            }
            catch( final Exception e ) {
                LOG.error( msg( "Error while reading stdout of monitored process \"{0}\"", server ), e );
            }

            // if we get here, we're stopping or we had some kind of problem and it's time to kill the process...
            if( !stopping && process.isAlive() ) {
                process.destroyForcibly();
                try {
                    process.waitFor();
                    LOG.info( msg( "Process \"{0}\" terminated because of error while reading stdout", server ) );
                }
                catch( InterruptedException e1 ) {
                    LOG.error( msg( "Interrupted while waiting for process \"{0}\" to terminate", server ), e1 );
                }
            }
        }
    }
}
