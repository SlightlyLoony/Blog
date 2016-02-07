package com.slightlyloony.blog.templates.compiler.tokens;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Token {

    private final int line;
    private final int col;
    private final TokenType type;
    private final Object value;
    private final String text;


    public Token( final int _line, final int _col, final TokenType _type, final String _text, final Object _value ) {
        line = _line;
        col = _col;
        text = _text;
        type = _type;
        value = _value;
    }


    public Token( final int _line, final int _col, final TokenType _type, final String _text ) {
        this( _line, _col, _type, _text, null );
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


    public TokenType getType() {
        return type;
    }


    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "" + (line + 1) + ":" + (col + 1) + " " + type.toString() + ": " + text;
    }
}
