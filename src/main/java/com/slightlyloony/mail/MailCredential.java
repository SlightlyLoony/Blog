package com.slightlyloony.mail;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailCredential {

    public String user;
    public String password;


    public MailCredential( final String _user, final String _password ) {
        user = _user;
        password = _password;
    }
}
