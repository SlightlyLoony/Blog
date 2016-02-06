package com.slightlyloony.blog.templates.compiler.tokens;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TokenBase implements Token {

    protected int line;
    protected int col;
    protected String text;


    protected TokenBase( final int _line, final int _col, final String _text ) {
        line = _line;
        col = _col;
        text = _text;
    }


    public int getLine() {
        return line;
    }


    public int getCol() {
        return col;
    }


    public String getText() {
        return text;
    }
}
