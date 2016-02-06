package com.slightlyloony.blog.templates.compiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.templates.compiler.tokens.*;
import com.slightlyloony.blog.util.S;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
                        tokens.add( new StringToken( tokenLine, tokenCol, accumulator.toString() ) );
                    accumulator.setLength( 0 );
                    start = index;
                    inItem = !inItem;
                    tokenLine = line;
                    tokenCol = col;
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
            log.append( "Source contains an unclosed set of braces (\"{{\" not followed by \"}}\")" );

        if( start < index )
            tokens.add( new StringToken( tokenLine, tokenCol, working.substring( start, index ) ) );

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


    private void handleWhiteSpace() {
        // naught to do...
    }


    private void handleNumberLiteral() {

        // accumulate anything that looks like a number...
        StringBuilder sb = new StringBuilder();
        sb.append( accumulator.charAt( itemIndex ) );
        while( (++itemIndex < accumulator.length()) && isNumber( accumulator.charAt( itemIndex ) ) )
            sb.append( accumulator.charAt( itemIndex ) );

        // convert it (the only way to have a problem is if it overflows)...
        try {
            int num = Integer.parseInt( sb.toString() );
            tokens.add( new IntegerLiteralToken( tokenLine, tokenCol, sb.toString(), num ) );
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

        tokens.add( new StringLiteralToken( tokenLine,tokenCol, sb.toString(), sb.toString() ) );
        tokenCol += sb.length() + 1;
    }


    private void handleComma() {
        tokens.add( new CommaToken( tokenLine, tokenCol, "," ) );
    }


    private boolean isPathChar() {
        return true;
    }


    private boolean isWhiteSpace( final char _char ) {
        return (_char == ' ') || (_char == '\t') || (_char == '\n') || (_char == '\r');
    }


    private boolean isNumber( final char _char ) {
        return ((_char >= '0') && (_char <= '9'));
    }


    private boolean isNumberStart( final char _char ) {
        return (_char == '-') || ((_char >= '0') && (_char <= '9'));
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
        result.put( '\n', this::handleWhiteSpace     );
        result.put( ' ',  this::handleWhiteSpace     );
        result.put( '\t', this::handleWhiteSpace     );
        result.put( '\r', this::handleWhiteSpace     );
        result.put( '-',  this::handleNumberLiteral  );
        for( char c = '0'; c <= '9'; c++ )
            result.put( c, this::handleNumberLiteral );
        result.put( '"',  this::handleStringLiteral  );
        result.put( ',',  this::handleComma          );
        return result;
    }


    public String getLog() {
        return log.toString();
    }


    private interface Parser {
        void parse();
    }


    public static void main( final String[] args ) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy( new FileInputStream( new File( "/Users/tom/IdeaProjects/Blog/test.txt" ) ), baos );
        String source = S.fromUTF8( baos.toByteArray() );

        Tokenizer t = new Tokenizer();
        List<Token> result = t.tokenize( source );
        result.hashCode();
    }
}
