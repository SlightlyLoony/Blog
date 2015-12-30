package com.slightlyloony.mail;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailSenderTest {

    @Test
    public void simpleTest() throws FileNotFoundException {

        Mail mail = new Mail();
        mail.recipients = "tom@dilatush.com";
        mail.subject = "Mail sender test";
        mail.body = "What could this be?";

        MailCredential cred = new Gson().fromJson( new FileReader( "PrivateStuff/mailcredential.json" ), MailCredential.class );

        MailSender sender = new MailSender( cred );
        sender.send( mail );

        mail = new Mail();
        mail.recipients = "tom@dilatush.com";
        mail.subject = "Mail sender test number two";
        mail.body = "Salmon recipe";
        mail.attachmentFile = "/Users/tom/Desktop/Baked Salmon With Herbed Mayonnaise Recipe - Food.com.pdf";
        mail.attachmentName = "Salmon Recipe.pdf";
        sender.send( mail );
    }

}