package com.slightlyloony.common.mail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailFetcher {

    private static final Logger LOG = LogManager.getLogger();

    private static final int BUFFER_SIZE = 16 * 1024;

    private final MailCredential credential;


    public MailFetcher( final MailCredential _credential ) {
        credential = _credential;
    }


    public List<MailMessage> fetch() {

        // make our list of messages, to be filled in and returned by the rest of this method's code...
        List<MailMessage> result = new ArrayList<>();

        try {
            // create properties field
            Properties props = new Properties();
            props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.pop3.socketFactory.fallback", "false");
            props.put("mail.pop3.socketFactory.port", "995");
            props.put("mail.pop3.port", "995");
            props.put("mail.pop3.host", "imap.gmail.com");
            props.put("mail.store.protocol", "imaps");

            Session emailSession = Session.getDefaultInstance( props );

            // create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("imaps");

            store.connect("imap.gmail.com", credential.user, credential.password);

            // create the folder object and open it
            Folder emailFolder = store.getFolder( "INBOX" );
            emailFolder.open( Folder.READ_WRITE );

            // retrieve the messages from the folder in an array and print it
            Message[] messages = emailFolder.getMessages();
            LOG.info( msg( "Fetched {0} email message headers", messages.length) );

            for( Message message : messages ) {

                if( message instanceof MimeMessage ) {

                    try {
                        MimeMessage mm = (MimeMessage) message;
                        List<MailPart> parts = new ArrayList<>();
                        getParts( mm, parts );

                        MailMessage msg = new MailMessage();
                        if( (mm.getFrom() == null) || (mm.getFrom().length == 0) )
                            continue;
                        InternetAddress addr = (InternetAddress)(mm.getFrom()[0]);
                        msg.from = addr.getAddress();
                        msg.message = mm;
                        msg.parts = parts;
                        msg.subject = mm.getSubject();

                        result.add( msg );

                        LOG.info( msg( "Read email message with {0} parts, subject: {1}", parts.size(), msg.subject) );

                        // mark the message for deletion...
                        mm.setFlag(Flags.Flag.DELETED, true);
                    }

                    catch( MessagingException e ) {
                        LOG.warn( msg( "Problem while reading mail message" ), e );
                    }
                }
            }

            // close the store and folder objects
            emailFolder.close(false);
            store.close();

        }

        catch (NoSuchProviderException e) {
            LOG.error( msg( "Mail provider could not be instantiated" ), e );
        }

        catch( MessagingException e ) {
            LOG.error( msg( "Problem while fetching mail" ), e );
        }

        // return our result, even if it contains nothing at all...
        return result;
    }


    /*
     * Extracts the parts contained in the message received.  If any exceptions occur during the processing, the part being extracted is skipped.
     * The parts extracted are added to the given part list.  Note that this method may be recursive, if there's a multipart component to the
     * message (as there almost always is these days!).
     */
    private void getParts( Part _p, List<com.slightlyloony.common.mail.MailPart> _partList ) {

        try {
            // analyze the content type...
            ContentType ct = new ContentType( _p.getContentType() );

            // if we have a multipart construct, analyze the components recursively...
            if( "multipart".equalsIgnoreCase( ct.getPrimaryType() ) ) {
                Multipart multi = (Multipart) _p.getContent();
                for( int i = 0; i < multi.getCount(); i++ ) {
                    Part bp = multi.getBodyPart( i );
                    getParts( bp, _partList );
                }
                return;
            }

            // make an object to hold our part...
            MailPart mp = new MailPart();
            mp.type = ct.getBaseType();

            // get the content object...
            Object content = _p.getContent();

            // if we have a text part, save it as a string...
            if( "text".equalsIgnoreCase( ct.getPrimaryType() ) ) {

                // if our text is in string form, save it as a part...
                if( content instanceof String )
                    mp.text = (String) content;

                // otherwise, don't add this part at all...
                else
                    return;
            }

            // otherwise, we don't know what type it is, so just save it as bytes...
            else {

                // get the filename for our (assumed) image or attachment; for some other type, it might be null...
                mp.name = _p.getFileName();

                // if we were handed an input stream, then read the bytes of our content...
                if( content instanceof InputStream ) {


                    byte[] buf = new byte[BUFFER_SIZE];
                    BufferedInputStream is = new BufferedInputStream( (InputStream) content, buf.length );
                    ByteArrayOutputStream baos = new ByteArrayOutputStream( BUFFER_SIZE );
                    int read;
                    while( (read = is.read( buf )) > 0 ) {
                        baos.write( buf, 0, read );
                    }
                    mp.content = baos.toByteArray();
                }

                // otherwise, don't add this part at all...
                else
                    return;
            }

            // add our part to the list of parts...
            _partList.add( mp );

        }

        catch( Exception e ) {
            LOG.error( msg( "Problem while fetching mail part" ), e );
        }
    }
}
