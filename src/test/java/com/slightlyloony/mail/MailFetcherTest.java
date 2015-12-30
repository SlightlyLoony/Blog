package com.slightlyloony.mail;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.FileReader;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MailFetcherTest {

    @Test
    public void testFetch() throws Exception {

        MailCredential cred = new Gson().fromJson( new FileReader( "PrivateStuff/mailcredential.json" ), MailCredential.class );

        MailFetcher fetcher = new MailFetcher( cred );

        List<MailMessage> messages = fetcher.fetch();

        hashCode();

    }
}