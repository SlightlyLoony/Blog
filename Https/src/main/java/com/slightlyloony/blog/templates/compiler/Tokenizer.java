package com.slightlyloony.blog.templates.compiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.templates.compiler.tokens.Token;
import com.slightlyloony.blog.templates.compiler.tokens.TokenType;

import java.util.List;
import java.util.Map;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Tokenizer {

    private static Map<Character,Parser> parsers;

    private StringBuilder log;
    private StringBuilder accumulator;
    private String working;
    private int index;
    private int itemIndex;
    private int line;
    private int col;
    private int tokenLine;
    private int tokenCol;
    private String template;
    private List<Token> tokens;
    private int start;


    public Tokenizer() {
        log = new StringBuilder();
        if( parsers == null )
            parsers = getParsers();
    }


    public List<Token> tokenize( final String _template ) {

        if( _template == null )
            throw new HandlerIllegalArgumentException( "Missing template source argument" );

        template = _template;

        log.setLength( 0 );

        line = 0;
        col = 0;
        tokens = Lists.newArrayList();

        working = template;
        boolean inItem = false;
        start = 0;
        accumulator = new StringBuilder();

        for( index = 0; index < template.length(); index++ ) {

            char c = template.charAt( index );

            if( c == (inItem ? '}' : '{') ) {
                int hits = scanChar( inItem ? '}' : '{' );
                col += hits;

                // if we detected an open or close item (pair of open or close braces)...
                if( hits == 2 ) {
                    accumulator.append( working.substring( start, index - 2 ) );
                    if( inItem )
                        handleTemplateItem();
                    else
                        if( accumulator.length() > 0 )
                            tokens.add( new Token( tokenLine, tokenCol, TokenType.String, accumulator.toString() ) );
                    accumulator.setLength( 0 );
                    start = index;
                    index--;
                    inItem = !inItem;
                    tokenLine = line;
                    tokenCol = col;
                    col--;
                }

                // else if we detected an escaped bunch of close braces...
                else if( hits >= 3 ) {
                    accumulator.append( working.substring( start, index - 1 ) );
                    start = index;
                }
            }

            // update our source position information...
            switch( c ) {

                case '\n':
                    col = 0;
                    line++;
                    break;

                case '\r':
                    col = 0;
                    break;

                case '\t':
                    col = 4 * ((col + 4) / 4);
                    break;

                default:
                    col++;
            }
        }

        if( inItem )
            logIssue( tokenLine, tokenCol, "Source contains an unclosed set of braces", "{{" );

        if( start < index )
            tokens.add( new Token( tokenLine, tokenCol - 2, TokenType.String, working.substring( start, index ) ) );

        return tokens;
    }


    /**
     * When invoked, the accumulator contains the item text, start has the index to the start of the token in the text, braces have been escaped,
     * and index points to the first character of the next token in the source.  One or more template items need to be parsed from the text in the
     * accumulator.
     */
    private void handleTemplateItem() {

        for( itemIndex = 0; itemIndex < accumulator.length(); itemIndex++ ) {

            char c = accumulator.charAt( itemIndex );
            Parser p = parsers.get( c );
            if( p != null )
                p.parse();

            // update our source position information...
            switch( c ) {

                case '\n':
                    tokenCol = 0;
                    tokenLine++;
                    break;

                case '\r':
                    tokenCol = 0;
                    break;

                case '\t':
                    tokenCol = 4 * ((tokenCol + 4) / 4);
                    break;

                default:
                    tokenCol++;
            }
        }
    }


    private void handleMinus() {

        // if there is no next character, this is an error...
        if( itemIndex >= accumulator.length() - 1 ) {
            logIssue( tokenLine, tokenCol, "Dangling minus sign", "-" );
            return;
        }

        // if the next character is another minus sign, we've got a decrement operator...
        char c = accumulator.charAt( itemIndex + 1 );
        if( c == '-' ) {
            tokens.add( new Token( tokenLine, tokenCol, TokenType.Dec, "--" ) );
            itemIndex++;
            return;
        }

        // or if the next character is a digit, we've got a negative number...
        if( isNumber( c ) ) {
            handleNumberLiteral();
            return;
        }

        // otherwise we've got an error...
        logIssue( tokenLine, tokenCol, "Invalid minus sign", "-" + c );
    }


    private void handlePlus() {

        // if there is no next character, this is an error...
        if( itemIndex >= accumulator.length() - 1 ) {
            logIssue( tokenLine, tokenCol, "Dangling plus sign", "+" );
            return;
        }

        // if the next character is another minus sign, we've got a decrement operator...
        char c = accumulator.charAt( itemIndex + 1 );
        if( c == '+' ) {
            tokens.add( new Token( tokenLine, tokenCol, TokenType.Inc, "++" ) );
            itemIndex++;
            return;
        }

        // otherwise we've got an error...
        logIssue( tokenLine, tokenCol, "Invalid plus sign", "+" + c );
    }


    private void handleNumberLiteral() {

        // accumulate anything that looks like a number...
        StringBuilder sb = new StringBuilder();
        sb.append( accumulator.charAt( itemIndex ) );
        while( (++itemIndex < accumulator.length()) && isNumber( accumulator.charAt( itemIndex ) ) )
            sb.append( accumulator.charAt( itemIndex ) );

        // convert it (the only way to have a problem is if it overflows)...
        try {
            tokens.add( new Token( tokenLine, tokenCol, TokenType.IntegerLiteral, sb.toString(), Integer.parseInt( sb.toString() ) ) );
        }
        catch( NumberFormatException e ) {
            logIssue( tokenLine, tokenCol, "Invalid number", sb.toString() );
        }

        tokenCol += sb.length() - 1;
        itemIndex--;
    }


    private void handleStringLiteral() {

        // accumulate everything up to the next unescaped double quote, or the end of line (which is an error)...
        StringBuilder sb = new StringBuilder();
        boolean good = false;
        while( ++itemIndex < accumulator.length() ) {

            char a = accumulator.charAt( itemIndex - 1 );
            char b = accumulator.charAt( itemIndex );

            if( b == '\n' )
                break;

            if( (b == '"') && (a != '\\') ) {
                good = true;
                break;
            }

            sb.append( b );
        }

        if( !good ) {
            logIssue( tokenLine, tokenCol, "Unterminated string literal", sb.toString() );
            return;
        }

        tokens.add( new Token( tokenLine,tokenCol, TokenType.StringLiteral, sb.toString(), sb.toString() ) );
        tokenCol += sb.length() + 1;
    }


    /**
     * A "word start" can be the beginning of a path, a boolean literal, a function name, or a directive.  Here we figure out only if it's a boolean
     * literal; for everything else we just record it as a "word" and the compiler sorts out the rest.
     */
    private void handleWordStart() {

        // accumulate everything that looks like a word...
        StringBuilder sb = new StringBuilder();
        sb.append( accumulator.charAt( itemIndex ) );
        while( (++itemIndex < accumulator.length()) && isWord( accumulator.charAt( itemIndex ) ) )
            sb.append( accumulator.charAt( itemIndex ) );

        // if we have a boolean literal, emit that...
        String result = sb.toString();
        if( "true".equals( result ) || "false".equals( result ) )
            tokens.add( new Token( tokenLine, tokenCol, TokenType.BooleanLiteral, result, "true".equals( result ) ) );
        else
            tokens.add( new Token( tokenLine, tokenCol, TokenType.Word, result, result ) );

        tokenCol += sb.length() - 1;
        itemIndex--;
    }


    private void handleComma() {
        tokens.add( new Token( tokenLine, tokenCol, TokenType.Comma, "," ) );
    }


    private void handleEqual() {
        tokens.add( new Token( tokenLine, tokenCol, TokenType.Equal, "=" ) );
    }


    private void handleOpenParen() {
        tokens.add( new Token( tokenLine, tokenCol, TokenType.OpenParen, "(" ) );
    }


    private void handleCloseParen() {
        tokens.add( new Token( tokenLine, tokenCol, TokenType.CloseParen, ")" ) );
    }


    private boolean isWord( final char _char ) {
        return ((_char >= 'a') && (_char <= 'z')) || ((_char >= 'A') && (_char <= 'Z')) || ((_char >= '0') && (_char <= '9'))
                || (_char == '_') || (_char == '.');
    }


    private boolean isNumber( final char _char ) {
        return ((_char >= '0') && (_char <= '9'));
    }


    private int scanChar( final char _char ) {
        int result = 1;
        while( (++index < working.length()) && (_char == working.charAt( index )) )
            result++;
        return result;
    }


    private void logIssue( final int _line, final int _col, final String _msg, final String _detail ) {
        log.append( "Line " );
        log.append( _line + 1 );
        log.append( ", column " );
        log.append( _col + 1 );
        log.append( ": " );
        log.append( _msg );
        log.append( " (" );
        log.append( _detail );
        log.append( ")\n" );
    }


    private Map<Character,Parser> getParsers() {
        Map<Character,Parser> result = Maps.newHashMap();
        result.put( '-',  this::handleMinus          );  // might be either a negative number or a decrement...
        for( char c = '0'; c <= '9'; c++ )
            result.put( c, this::handleNumberLiteral );
        result.put( '"',  this::handleStringLiteral  );
        result.put( ',',  this::handleComma          );
        result.put( '+',  this::handlePlus           );
        result.put( '=',  this::handleEqual          );
        result.put( '(',  this::handleOpenParen      );
        result.put( ')',  this::handleCloseParen     );
        result.put( '.',  this::handleWordStart      );  // might be a boolean literal, a path, a function, or a directive...
        for( char c = 'a'; c <= 'z'; c++ )
            result.put( c, this::handleWordStart     );
        for( char c = 'A'; c <= 'Z'; c++ )
            result.put( c, this::handleWordStart     );
        return result;
    }


    public String getLog() {
        return log.toString();
    }


    private interface Parser {
        void parse();
    }
}
