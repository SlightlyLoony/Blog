package com.slightlyloony.mail;

import com.slightlyloony.logging.LU;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailSender {

    private static final Logger LOG = LogManager.getLogger();


    private final MailCredential credential;


    public MailSender( final MailCredential _credential ) {
        credential = _credential;
    }


    public boolean send( final MailMessage _mail ) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication( credential.user, credential.password );
                    }
                });

        try {

            Message message = new MimeMessage( session );

            // Set From: header field of the header.
            message.setFrom( new InternetAddress( credential.user ) );

            // Set To: header field of the header.
            message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( _mail.recipients ));

            // Set Subject: header field
            message.setSubject( _mail.subject );

            // iterate over all the message parts, adding them to the content...
            Multipart multipart = new MimeMultipart();
            for( MailPart part : _mail.parts ) {
                MimeBodyPart mbp = new MimeBodyPart();
                if( "text/plain".equals( part.type ) )
                    mbp.setText( part.text );
                else {
                    mbp.setContent( part.content, part.type );
                    mbp.setFileName( part.name );
                }
                multipart.addBodyPart( mbp );
            }
            message.setContent(multipart);

            LOG.info( LU.msg( "Sending mail to {0}, subject: {1}", _mail.recipients, _mail.subject) );
            Transport.send( message );
            LOG.info( LU.msg( "Sent mail to {0}, subject: {1}", _mail.recipients, _mail.subject) );

            return true;

        }

        catch( MessagingException e ) {
            LOG.error( "Problem sending email", e );
            return false;
        }
    }
}
