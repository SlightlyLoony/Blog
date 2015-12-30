package com.slightlyloony.mail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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

    private final MailCredential credential;


    public MailSender( final MailCredential _credential ) {
        credential = _credential;
    }


    public boolean send( final Mail _mail ) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance( props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication( credential.user, credential.password );
                    }
                } );

        try {

            Message message = new MimeMessage( session );

            // Set From: header field of the header.
            message.setFrom( new InternetAddress( credential.user ) );

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse( _mail.recipients ));

            // Set Subject: header field
            message.setSubject( _mail.subject );

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText( _mail.body );

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);


            if( _mail.attachmentFile != null ) {

                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource( _mail.attachmentFile );
                messageBodyPart.setDataHandler( new DataHandler( source ) );
                messageBodyPart.setFileName( _mail.attachmentName );
                multipart.addBodyPart( messageBodyPart );
            }

            // Send the complete message parts
            message.setContent(multipart);

            Transport.send( message );

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
