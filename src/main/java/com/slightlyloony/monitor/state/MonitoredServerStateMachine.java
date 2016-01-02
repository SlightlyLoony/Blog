package com.slightlyloony.monitor.state;

import com.google.common.collect.ImmutableSet;
import com.slightlyloony.monitor.MonitoredProcess;

import java.util.Set;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitoredServerStateMachine extends StateMachine {


    private MonitoredProcess process;


    public MonitoredServerStateMachine( final String _name, final MonitoredProcess _process ) {
        super( _name );
        process = _process;
    }


    public MonitoredProcess getProcess() {
        return process;
    }


    @Override
    protected Set<Class<? extends State>> initAllowedStates() {
        return ImmutableSet.of( DeadState.class, AliveState.class, ErrorState.class );
    }


    @Override
    protected State initState() {
        return new DeadState( this );
    }
}
