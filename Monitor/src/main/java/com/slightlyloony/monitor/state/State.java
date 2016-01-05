package com.slightlyloony.monitor.state;

import com.slightlyloony.common.ExecutionService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class State {

    protected final StateMachine parent;
    protected final StateMachine child;


    protected State( final StateMachine _parent, final StateMachine _child ) {

        if( _parent == null )
            throw new IllegalArgumentException( "Missing parent state machine argument" );

        parent = _parent;
        child = _child;  // null is a valid value for the case when there are no substates...
    }


    public void on( final Event _event, final Object... _data ) {

        // sanity check...
        if( _event == null )
            throw new IllegalArgumentException( "Event argument is null in on() of \"" + toString() + "\"" );

        // if we can handle the event in this state, we're done...
        if( handleEvent( _event, _data ))
            return;

        // otherwise, if we have a child state machine we'll try there...
        if( child != null ) {
            child.on( _event, _data );
            return;
        }

        // otherwise, we don't know how to handle this one!
        throw new IllegalArgumentException( "Unexpected event " + _event.name() + " in \"" + toString() + "\"" );
    }


    protected ScheduledFuture delayed( final long _delay, final TimeUnit _timeUnit, final Event _event, final Object... _data ) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                parent.on( _event, _data );
            }
        };
        return ExecutionService.INSTANCE.schedule( runnable, _delay, _timeUnit );
    }


    /**
     * The parent state machine calls this method to handle the given event (with the given optional data).  If this state can handle the event,
     * do so and return true (to indicate that the event was handled).  Otherwise, do nothing and return false.
     *
     * @param _event the event to be handled.
     * @param _data any data associated with the event.
     * @return true if the event was handled.
     */
    abstract protected boolean handleEvent( final Event _event, final Object... _data );


    /**
     * This method is called just before the parent state machine transitions <i>from</i> this state to another.  Override this method to take
     * action at that time.
     */
    protected void from() {
        // do nothing by default...
    }


    /**
     * This method is called just after the parent state machine transitions <i>to</i> this state from another.  Override this method to take
     * action at that time.
     */
    protected void to() {
        // do nothing by default...
    }


    @Override
    public String toString() {
        return parent.toString() + ":" + getClass().getSimpleName();
    }
}
