package com.slightlyloony.monitor.state;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class AliveState extends State {

    protected AliveState( final StateMachine _parent ) {
        super( _parent, null );
    }


    /**
     * The parent state machine calls this method to handle the given event (with the given optional data).  If this state can handle the event, do so
     * and return true (to indicate that the event was handled).  Otherwise, do nothing and return false.
     *
     * @param _event the event to be handled.
     * @param _data  any data associated with the event.
     * @return true if the event was handled.
     */
    @Override
    protected boolean handleEvent( final Event _event, final Object... _data ) {
        return false;
    }
}
