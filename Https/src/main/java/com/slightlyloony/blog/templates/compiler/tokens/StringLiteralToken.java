package com.slightlyloony.blog.templates.compiler.tokens;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StringLiteralToken extends TokenBase implements Token {

    private String str;


    public StringLiteralToken( final int _line, final int _col, final String _text, String _str ) {
        super( _line, _col, _text );
        str = _str;
    }


    public String getStr() {
        return str;
    }
}
