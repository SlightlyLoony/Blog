package com.slightlyloony.blog.templates.tests;

/**
 * Implemented by classes that provide tests (for control of logical program flow) directives.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Test {

    /**
     * Returns true if this test is satisfied, or false if it is not.
     *
     * @return true if this test is satisfied, or false if it is not
     */
    boolean test();
}
