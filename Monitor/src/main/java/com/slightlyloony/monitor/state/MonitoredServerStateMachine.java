package com.slightlyloony.monitor.state;

import com.google.common.collect.ImmutableSet;
import com.slightlyloony.common.ipmsgs.IPMsgParticipant;
import com.slightlyloony.monitor.MonitoredProcess;

import java.util.Set;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MonitoredServerStateMachine extends StateMachine {


    protected MonitoredProcess process;
    protected IPMsgParticipant participant;


    public MonitoredServerStateMachine( final IPMsgParticipant _participant, final MonitoredProcess _process ) {
        super( (_participant != null) ? _participant.name() : "" );

        if( (_participant == null) || (_process == null))
            throw new IllegalArgumentException( "MonitoredServerStateMachine constructor missing parameters" );

        process = _process;
        participant = _participant;
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
