package com.slightlyloony.monitor.state;

import com.slightlyloony.monitor.MonitoredProcess;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class MonitoredProcessState extends State {

    protected MonitoredProcessState( final StateMachine _parent, final StateMachine _child ) {
        super( _parent, _child );
    }


    protected MonitoredProcess getProcess() {
        return ((MonitoredServerStateMachine) parent).getProcess();
    }
}
