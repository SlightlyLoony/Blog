package com.slightlyloony.monitor.state;

import com.slightlyloony.monitor.MonitoredProcess;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class MonitoredProcessState extends State {

    protected MonitoredServerStateMachine parent;

    protected MonitoredProcessState( final MonitoredServerStateMachine _parent, final StateMachine _child ) {
        super( _parent, _child );
        parent = _parent;
    }


    protected MonitoredProcess getProcess() {
        return ((MonitoredServerStateMachine) parent).getProcess();
    }
}
