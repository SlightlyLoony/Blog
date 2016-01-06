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


    @Override
    public synchronized void on( final Event _event, Object... _data ) {

        // handle the INITIALIZE event specially if we have no current state...
        if( (state == null) && (_event == Event.INITIALIZE) )
            to( new DeadState( this ) );

        // now delegate (including INITIALIZE) to states...
        super.on( _event, _data );
    }


    public MonitoredProcess getProcess() {
        return process;
    }


    @Override
    protected Set<Class<? extends State>> initAllowedStates() {
        return ImmutableSet.of( DeadState.class, AliveState.class, ErrorState.class, RestartingState.class );
    }


    @Override
    public String toString() {
        return (participant == null) ? super.toString() : participant.toString();
    }
}
