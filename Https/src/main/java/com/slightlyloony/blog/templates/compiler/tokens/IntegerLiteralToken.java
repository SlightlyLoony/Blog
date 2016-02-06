package com.slightlyloony.blog.templates.compiler.tokens;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IntegerLiteralToken extends TokenBase implements Token {

    private int integer;


    public IntegerLiteralToken( final int _line, final int _col, final String _text, int _int ) {
        super( _line, _col, _text );
        integer = _int;
    }


    public int getInt() {
        return integer;
    }
}
