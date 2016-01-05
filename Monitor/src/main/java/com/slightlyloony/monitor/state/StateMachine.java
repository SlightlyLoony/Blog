package com.slightlyloony.monitor.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

import static com.slightlyloony.common.logging.LU.msg;

/**
 * Generalized state machine.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class StateMachine {

    private static Logger LOG = LogManager.getLogger();


    protected final Set<Class<? extends State>> allowedStates;
    protected final String name;
    protected State state;


    public StateMachine( final String _name ) {
        name = _name;
        allowedStates = initAllowedStates();
        transitionTo( initState() );
    }


    abstract protected Set<Class<? extends State>> initAllowedStates();


    abstract protected State initState();


    public synchronized void on( final Event _event, final Object... _data ) {
        state.on( _event, _data );
    }


    protected void transitionTo( final State _state ) {
        from();
        to( _state );
    }


    protected void from() {
        if( state != null )
            state.from();
    }


    protected void to( final State _state ) {

        // make sure the new state is a valid one...
        if( (_state == null) || !allowedStates.contains( _state.getClass() )) {
            String stateType = (_state == null) ? "null" : _state.toString();
            LOG.error( msg( "Attempted to transition to invalid state {0} in {1}", stateType, toString() ) );
            throw new IllegalArgumentException( "State \"" + stateType + "\" is not allowed in state machine \"" + toString() + "\"");
        }

        // log our transition...
        if( state != null)
            LOG.info( msg( "Transitioning from {1} to {0}", _state.toString(), state.toString() ) );
        else
            LOG.info( msg( "Initial transition to {0}", _state.toString() ) );

        // tell our old state that we're leaving...
        if( state != null )
            state.from();

        // it's ok, so do it...
        state = _state;
        state.to();
    }


    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
