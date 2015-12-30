package com.slightlyloony.mail;

import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailMessage {

    public String from;
    public String subject;
    public List<MailPart> parts;
    public MimeMessage message;
}
