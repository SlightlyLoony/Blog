package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.templates.sources.RootSource;
import com.slightlyloony.blog.users.User;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Template {

    private RootSource source;
    private User user;
    private TemplateElements elements;


    public Template() {
        elements = new TemplateElements();
    }


    public void reset() {
        elements.reset();
    }


    public TemplateInputStream render() {
        reset();
        return new TemplateInputStream( this );
    }


    public void add( final TemplateElement _element ) {
        elements.add( _element );
    }


    public RootSource getSource() {
        return source;
    }


    public void setSource( final RootSource _source ) {
        source = _source;
    }


    public TemplateElements getElements() {
        return elements;
    }


    public User getUser() {
        return user;
    }


    public void setUser( final User _user ) {
        user = _user;
    }
}
