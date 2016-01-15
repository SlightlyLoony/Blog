package com.slightlyloony.blog.responders;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum ResponderType {

    BLOG_OBJECT( BlogObjectResponder::new );


    private GetResponder getResponder;


    ResponderType( final GetResponder _getResponder ) {
        getResponder = _getResponder;
    }


    public Responder getResponder() {
        return getResponder.get();
    }


    private static interface GetResponder {
        Responder get();
    }
}
