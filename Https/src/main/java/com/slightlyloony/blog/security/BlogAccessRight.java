package com.slightlyloony.blog.security;

/**
 * Defines the specific rights that a blog user may have, or that a blog object may require for access.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum BlogAccessRight {

    MANAGER,
    AUTHOR,
    REVIEWER,
    AUTHENTICATED,
    SESSION,
    ADULT,
    PUBLIC;
}
