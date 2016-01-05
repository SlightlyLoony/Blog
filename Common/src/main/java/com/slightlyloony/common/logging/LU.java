package com.slightlyloony.common.logging;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFormatMessage;

/**
 * Static container class for utility methods related to logging with Apache log4j 2.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class LU {

    public static Message msg( final String _pattern, final Object... _params ) {
        return new MessageFormatMessage( _pattern, _params );
    }

}
