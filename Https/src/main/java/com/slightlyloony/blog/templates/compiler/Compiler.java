package com.slightlyloony.blog.templates.compiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.ServerInit;
import com.slightlyloony.blog.templates.*;
import com.slightlyloony.blog.templates.compiler.tokens.Token;
import com.slightlyloony.blog.templates.functions.Function;
import com.slightlyloony.blog.templates.functions.FunctionDef;
import com.slightlyloony.blog.templates.sources.Path;
import com.slightlyloony.blog.templates.sources.data.*;
import com.slightlyloony.blog.util.S;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Compiler {

    private StringBuilder log;
    private Deque<Value> valueStack;
    private Deque<Segment> segmentStack;
    private boolean html;
    private boolean css;
    private String lastWord;


    public Template compile( final String _source ) {

        // setup...
        log = new StringBuilder();
        valueStack = Queues.newArrayDeque();
        segmentStack = Queues.newArrayDeque();
        html = false;
        css = false;
        lastWord = null;

        // add the base segement for the template itself...
        segmentStack.add( new Segment( SegmentType.Template, Lists.newArrayList() ) );

        // first we tokenize it...
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize( _source );

        // if there was an error log from tokenizing, record it...
        String tokenizingErrors = tokenizer.getLog();
        if( tokenizingErrors.length() > 0 )
            log.append( tokenizingErrors );

        // compile the tokens we got...
        for( Token token : tokens ) {

            switch( token.getType() ) {

                case String:         handleString( token );         break;
                case Comma:          break;
                case OpenParen:      handleOpenParen( token );      break;
                case CloseParen:     handleCloseParen( token );     break;
                case Word:           handleWord( token );           break;
                case IntegerLiteral: handleIntegerLiteral( token ); break;
                case BooleanLiteral: handleBooleanLiteral( token ); break;
                case StringLiteral:  handleStringLiteral( token );  break;
                case Inc:            break;
                case Dec:            break;
                case Equal:          break;
                case Else:           break;
                case End:            handleEnd( token );            break;
                case HTML:           break;
                case CSS:            break;
                default:
            }
        }

        return null;
    }


    private void handleEnd( final Token _token ) {

        // if the segment stack is empty, we've got a real problem (too many "ends")...
        if( segmentStack.size() < 2 ) {
            logIssue( _token, "Too many 'end' directives", "end" );
            return;
        }

        // construct our control element and add it to the next higher level segment...
        Segment segment = segmentStack.removeLast();
        switch( segment.type ) {

            case IfElse:
                TemplateElements posElements = new TemplateElements( segment.hadElse ? segment.altElements : segment.elements );
                TemplateElements negElements = new TemplateElements( segment.hadElse ? segment.elements : segment.altElements );
                addElement( new IfElseTemplateElement( segment.datum, posElements, negElements ) );
                break;

            case While:
                addElement( new WhileTemplateElement( segment.datum, new TemplateElements( segment.elements ) ) );
                break;

            case Foreach:
                if( segment.datum instanceof PathDatum ) {
                    PathDatum pathDatum = (PathDatum) segment.datum;
                    addElement( new ForEachTemplateElement( (Path) pathDatum.getValue(), new TemplateElements( segment.elements ) ) );
                }
                else
                    logIssue( _token, "Argument for foreach() is not a path", "" );
                break;

            default:
                // it should not be possible to get here...
        }
    }


    private void handleWord( final Token _token ) {
        lastWord = (String) _token.getValue();
    }


    private void handleIntegerLiteral( final Token _token ) {

        // if we had a preceding word, then it must be a path...
        ifWordMakePath( _token );

        // now add our literal as an element or value...
        addElementOrValue( new IntegerDatum( (Integer) _token.getValue() ) );
    }


    private void handleBooleanLiteral( final Token _token ) {

        // if we had a preceding word, then it must be a path...
        ifWordMakePath( _token );

        // now add our literal as an element or value...
        addElementOrValue( new BooleanDatum( (Boolean) _token.getValue() ) );
    }


    private void handleStringLiteral( final Token _token ) {

        // if we had a preceding word, then it must be a path...
        ifWordMakePath( _token );

        // now add our literal as an element or value...
        addElementOrValue( new StringDatum( (String) _token.getValue() ) );
    }


    private void handleOpenParen( final Token _token ) {

        // if there's no last word, we've got an error...
        if( lastWord == null ) {
            logIssue( _token, "Open parentheses with no preceding word", "(" );
            return;
        }

        // the preceding word could be either a control statement or a function...
        switch( lastWord ) {

            case "if":      handleControls( _token, SegmentType.IfElse );  break;
            case "while":   handleControls( _token, SegmentType.While );   break;
            case "foreach": handleControls( _token, SegmentType.Foreach ); break;
            default:        handleFunctions( _token );
        }
    }


    private void handleCloseParen( final Token _token ) {

        // if there's a last word, it's a path...
        ifWordMakePath( _token );

        // if there are no values on the stack, then we have a bad situation (too many close parens)...
        if( valueStack.size() == 0 ) {
            logIssue( _token, "Unexpected close parentheses", ")" );
            return;
        }

        // walk down the stack accumulating data until we hit something else...
        List<Datum> data = Lists.newArrayList();
        while( valueStack.peekLast().datum != null )
            data.add( valueStack.removeLast().datum );

        // get the next item, and see what we got...
        Value value = valueStack.removeLast();

        // if it's a function, validate it and push it back as a datum...
        if( value.type != null ) {

            // get the data as an argument array...
            Datum[] args = data.toArray( new Datum[data.size()] );

            // validate this function; log the errors if there are any...
            String errs = Function.validate( value.type, args );

            // if we have the wrong number of arguments, complain and stuff a boolean false instead of the function...
            if( errs != null ) {
                logIssue( _token, "Invalid number of arguments: " + errs, "" );
                valueStack.add( new Value( null, new BooleanDatum( false ), false ) );
            }

            // otherwise, all is well and we need to push our function datum down...
            else
                valueStack.add( new Value( null, Function.create( value.type, args ), false ) );

            // if the value stack now has just one entry, the value needs to be popped to become a template element...
            if( valueStack.size() == 1 ) {
                value = valueStack.removeLast();
                addElement( new DatumTemplateElement( value.datum ) );
            }
        }

        // otherwise, it's a control statement and we need to add the value to our segment...
        else {

            // we should have exactly one value in the data; otherwise log errors and use the first one...
            if( data.size() != 1 )
                logIssue( _token, "Wrong number of arguments for control element", "not 1" );

            // if we have no datum, stuff a false...
            if( data.size() == 0 )
                data.add( new BooleanDatum( false ) );

            // stuff our datum into the segment...
            segmentStack.peekLast().datum = data.get( 0 );
        }
    }


    private void handleControls( final Token _token, final SegmentType _type ) {

        // if the value stack isn't empty, we have a problem...
        if( valueStack.size() > 0 ) {
            logIssue( _token, "Control element used as a function", _type.toString() );
            return;
        }

        // we need to push a new segment, and a control marker on the value stack...
        valueStack.add( new Value( null, null, true ) );
        segmentStack.add( new Segment( _type, Lists.newArrayList() ) );

        lastWord = null;
    }


    private void handleFunctions( final Token _token ) {

        // if the preceding word isn't a function, we've got a problem...
        FunctionDef def = FunctionDef.getByName( lastWord );
        if( def == null)
            logIssue( _token, "Invalid function name", lastWord );

        // otherwise, push the function onto the value stack...
        else
            valueStack.add( new Value( def, null, false ) );

        lastWord = null;
    }


    private void handleString( final Token _token ) {

        // if we had a preceding word, then it must be a path...
        ifWordMakePath( _token );

        String text = (String) _token.getValue();

        // do any stripping we need, depending on our mode...
        if( html ) text = htmlStrip( text );
        if( css  ) text = cssStrip( text );

        addElement( new StringTemplateElement( text ) );
    }


    private void ifWordMakePath( final Token _token ) {

        if( lastWord == null )
            return;

        // validate the last word as a path...
        String errs = Path.validate( lastWord );
        if( errs == null )
            addElementOrValue( new PathDatum( Path.create( lastWord ) ) );
        else
            logIssue( _token, "Invalid path: " + errs, lastWord );

        lastWord = null;
    }


    private void addElementOrValue( final Datum _datum ) {

        if( valueStack.size() > 0 )
            valueStack.add( new Value( null, _datum, false ) );

            // otherwise, make a new template element...
        else
            addElement( new DatumTemplateElement( _datum ) );

    }


    private void addElement( final TemplateElement _element ) {
        segmentStack.peekLast().elements.add( _element );
    }


    private String htmlStrip( final String _text ) {
        return _text;
    }


    private String cssStrip( final String _text ) {
        return _text;
    }


    private void logIssue( final Token _token, final String _msg, final String _detail ) {
        log.append( "Line " );
        log.append( _token.getLine() + 1 );
        log.append( ", column " );
        log.append( _token.getCol() + 1 );
        log.append( ": " );
        log.append( _msg );
        log.append( " (" );
        log.append( _detail );
        log.append( ")\n" );
    }


    private enum SegmentType { Template, IfElse, While, Foreach }


    private static class Segment {
        private SegmentType type;
        private List<TemplateElement> elements;
        private List<TemplateElement> altElements;
        private Datum datum;
        private boolean hadElse;


        public Segment( final SegmentType _type, final List<TemplateElement> _elements ) {
            type = _type;
            elements = _elements;
            datum = null;
            altElements = Lists.newArrayList();
            hadElse = false;
        }
    }


    private static class Value {
        FunctionDef type;
        Datum datum;
        boolean control;


        public Value( final FunctionDef _type, final Datum _datum, final boolean _control ) {
            type = _type;
            datum = _datum;
            control = _control;
        }
    }


    // TODO: remove this testing stub...
    public static void main( final String[] args ) throws IOException {

        // configure the logging properties file...
        System.getProperties().setProperty( "log4j.configurationFile", "log.xml" );

        // initialize the blog application...
        ServerInit.init();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy( new FileInputStream( new File( "/Users/tom/IdeaProjects/Blog/test.txt" ) ), baos );
        String source = S.fromUTF8( baos.toByteArray() );

        Compiler c = new Compiler();
        Template t = c.compile( source );

        c.hashCode();
    }
}
