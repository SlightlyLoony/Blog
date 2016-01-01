package com.slightlyloony.monitor;

import com.slightlyloony.common.ExecutionService;
import com.slightlyloony.mail.MailFetcher;
import com.slightlyloony.mail.MailMessage;
import com.slightlyloony.mail.MailPart;
import com.slightlyloony.mail.MailSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.slightlyloony.logging.LU.msg;

/**
 * Checks mail at a configurable interval and processes the emails received.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailPortal implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    private static ScheduledFuture<?> portal;


    /**
     * Starts the mail portal if it has not already been started.
     */
    public static void start() {

        if( portal == null ) {
            int seconds = Init.getConfig().getMailPortalFetchIntervalSeconds();
            portal = ExecutionService.INSTANCE.scheduleAtFixedRate( new MailPortal(), 0, seconds, TimeUnit.SECONDS );
        }
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        LOG.info( "Mail portal started" );

        // fetch any messages we might have...
        MailFetcher fetcher = new MailFetcher( Init.getConfig().getMailCredential() );
        List<MailMessage> msgs = fetcher.fetch();

        // process any messages we received...
        for( MailMessage msg : msgs ) {

            // if it's not from our authorized portal user, then just ignore the email...
            if( !msg.from.equals( Init.getConfig().getMailPortalAuthorizedUser() )) {
                LOG.info( msg( "Ignoring message from {0} with subject \"{1}\"", msg.from, msg.subject ) );
                continue;
            }

            // decide what to do based on the subject...
            switch( msg.subject.toUpperCase() ) {

                case "HELP":  handleHELP( msg );    break;
                case "PING":  handlePING( msg );    break;
                default:      handleDefault( msg ); break;
            }
        }

        LOG.info( "Mail portal finished" );
    }


    private void handleHELP( final MailMessage _msg ) {
        String msg = "Blog Monitor Mail Portal Help\n" +
                     "  HELP to get this message\n" +
                     "  PING to get a pong response\n" +
                     "Anything else will be ignored, but logged";
        sendMessage( "Help, as requested...", msg );
    }


    private void handlePING( final MailMessage _msg ) {
        sendMessage( "Pong...", "You asked for this, you know..." );
    }


    private void handleDefault( final MailMessage _msg ) {
        LOG.info( msg( "Message with unexpected subject (\"{0}\") received", _msg.subject ) );
    }


    private void sendMessage( final String _subject, final String _body ) {

        // make our mail message...
        MailMessage msg = new MailMessage();
        msg.from = Init.getConfig().getMonitorEmailUser();
        msg.subject = _subject;
        msg.recipients = Init.getConfig().getMailPortalAuthorizedUser();
        MailPart mp = new MailPart();
        mp.type = "text/plain";
        mp.text = _body;
        msg.parts = new ArrayList<>();
        msg.parts.add( mp );

        // then send it...
        MailSender ms = new MailSender( Init.getConfig().getMailCredential() );
        ms.send( msg );
    }
}
