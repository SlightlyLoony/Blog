package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.templates.sources.RootSource;
import com.slightlyloony.blog.users.ImmutableUser;
import com.slightlyloony.blog.users.User;

/**
 * This class essential provides thread-specific global variables for use in template rendering.  I chose this design only after attempting three
 * times to implement a more conventional approach in which the necessary objects {@link RootSource} and {@link User}
 * were passed as arguments in methods.  This proved to be more cumbersome than I was comfortable with, littering the template class APIs with
 * references to these arguments in situations where doing so only made sense if one had a very deep understanding of how even the lowest levels
 * of the template classes worked.
 * <p>
 * Hence this approach with &ldquo;globals&rdquo;.  This class uses a {@link ThreadLocal} instance so that each thread that may be concurrently
 * rendering a template has its own context.  Because each template is rendered in a single HTTP request processing thread, this is safe and
 * effective.  To further increase the safety of this approach, the objects stored in this class are immutable.  This mainly serves as a way to have
 * the compiler detect any attempt to make changes through the APIs when the object is retrieved from this class; a determined hacker could find a
 * way around any such protection.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TemplateRenderingContext {

    private static final ThreadLocal<TemplateRenderingContext> INSTANCE = ThreadLocal.withInitial( () -> null );

    private final RootSource source;
    private final User user;


    private TemplateRenderingContext( final RootSource _source, final User _user ) {

        if( (_source == null) || (_user == null) )
            throw new HandlerIllegalArgumentException( "Missing required source or user argument" );

        // a source is immutable by design, so we just put it in directly...
        source = _source;

        // the user is not immutable by design, so we wrap it if necessary...
        user = (_user instanceof ImmutableUser) ? _user : new ImmutableUser( _user );
    }


    public RootSource getSource() {
        return source;
    }


    public User getUser() {
        return user;
    }


    public static void set( final RootSource _source, final User _user ) {
        INSTANCE.set( new TemplateRenderingContext( _source, _user ) );
    }


    public static TemplateRenderingContext get() {
        return INSTANCE.get();
    }


    public static void remove() {
        INSTANCE.remove();
    }
}
