package com.slightlyloony.blog.templates.tests;

import com.slightlyloony.blog.templates.sources.data.BooleanDatum;
import com.slightlyloony.blog.templates.sources.data.Datum;

import static com.slightlyloony.blog.templates.TemplateUtil.toInt;

/**
 * Defines all template functions in the system.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum TestDef implements TestAction {

    isZero   ( 1, 1, getIsZeroTest()     ),
    isNotZero( 1, 1, getIsNotZeroTest()  );


    private int minArgs;
    private int maxArgs;
    private TestAction testAction;


    TestDef( final int _minArgs, final int _maxArgs, final TestAction _action ) {
        minArgs = _minArgs;
        maxArgs = _maxArgs;
        testAction = _action;
    }


    public int getMinArgs() {
        return minArgs;
    }


    public int getMaxArgs() {
        return maxArgs;
    }


    public Datum test( final Datum... _arguments ) {
        return testAction.test( _arguments );
    }


    public TestAction getAction() {
        return testAction;
    }


    private static TestAction getIsZeroTest() { return _arguments -> {
            return new BooleanDatum( toInt( _arguments[0] ) == 0 );
        };
    }


    private static TestAction getIsNotZeroTest() { return _arguments -> {
        return new BooleanDatum( toInt( _arguments[0] ) != 0 );
        };
    }
}
