package com.slightlyloony.mail;

import com.slightlyloony.monitor.MonitorInit;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailSenderTest {

    @Test
    public void test() {

        MonitorInit.init();

        // make our mail message...
        MailMessage msg = new MailMessage();
        msg.from = MonitorInit.getConfig().getMonitorEmailUser();
        msg.subject = "Subject";
        msg.recipients = MonitorInit.getConfig().getMailPortalAuthorizedUser();
        MailPart mp = new MailPart();
        mp.type = "text/plain";
        mp.text = "body";
        msg.parts = new ArrayList<>();
        msg.parts.add( mp );

        // then send it...
        MailSender ms = new MailSender( MonitorInit.getConfig().getMailCredential() );
        ms.send( msg );

    }
}