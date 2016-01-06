package com.slightlyloony.monitor.state;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum Event {

    INITIALIZE,
    ALIVE,
    IS_ALIVE_CHECK,
    WEB_ALIVE,
    IS_WEB_ALIVE_CHECK,
    WEB_TEST_SUCCESS,
    WEB_TEST_FAILURE,
    RESTART,
    PROCESS_DEAD,
    SHUTDOWN;

}
