package com.slightlyloony.blog.templates.compiler.tokens;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StringToken extends TokenBase implements Token {


    public StringToken( final int _line, final int _col, final String _string ) {
        super( _line, _col, _string );
    }
}
