package com.slightlyloony.common.ipmsgs;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface IPMsgAction {

    public void run( final IPMsgParticipant _sender, final IPData _data );
}
