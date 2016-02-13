package com.slightlyloony.blog.templates.compiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.slightlyloony.blog.templates.*;
import com.slightlyloony.blog.templates.compiler.tokens.Token;
import com.slightlyloony.blog.templates.compiler.tokens.TokenType;
import com.slightlyloony.blog.templates.functions.Function;
import com.slightlyloony.blog.templates.functions.FunctionDef;
import com.slightlyloony.blog.templates.sources.Path;
import com.slightlyloony.blog.templates.sources.data.*;

import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TemplateCompiler {

    private static final String BIV_LOG = "template_compiler_log";
    private static final String BIV_LOG_NOT_EMPTY = "template_compiler_log_not_empty";

    private StringBuilder log;
    private Deque<Value> valueStack;
    private Deque<Segment> segmentStack;
    private boolean html;
    private boolean css;
    private String lastWord;
    private boolean lastComma;


    public Template compile( final String _source ) {

        // setup...
        log = new StringBuilder();
        valueStack = Queues.newArrayDeque();
        segmentStack = Queues.newArrayDeque();
        html = false;
        css = false;
        lastWord = null;
        lastComma = false;

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

            handleStateTransitions( token );

            switch( token.getType() ) {

                case String:         handleString         ( token ); break;
                case Comma:          handleComma          ( token ); break;
                case OpenParen:      handleOpenParen      ( token ); break;
                case CloseParen:     handleCloseParen     ( token ); break;
                case Word:           handleWord           ( token ); break;
                case IntegerLiteral: handleIntegerLiteral ( token ); break;
                case BooleanLiteral: handleBooleanLiteral ( token ); break;
                case StringLiteral:  handleStringLiteral  ( token ); break;
                case Inc:            handleInc            ( token ); break;
                case Dec:            handleDec            ( token ); break;
                case Equal:          handleEqual          ( token ); break;
                case Else:           handleElse           ( token ); break;
                case End:            handleEnd            ( token ); break;
                case HTML:           handleHTML           (       ); break;
                case CSS:            handleCSS            (       ); break;
                case LOG:            handleLOG            ( token ); break;
                default:
            }
        }

        // handle transitions to a fake empty string, just to clear out anything lingering behind...
        handleStateTransitions( new Token( 0, 0, TokenType.String, "" ) );

        // when we get here, there should be just one entry on our segment stack, and none on the value stack...
        if( valueStack.size() != 0 )
            logIssue( tokens.get( tokens.size() - 1 ), "Unexpected value remaining on value stack", "" + valueStack.size() );
        if( segmentStack.size() != 1 )
            logIssue( tokens.get( tokens.size() - 1 ), "Unexpected segment stack size after compilation", "" + segmentStack.size() );

        return new Template( new TemplateElements( segmentStack.getFirst().elements ) );
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
                addElement( _token, new IfElseTemplateElement( segment.datum, posElements, negElements ) );
                break;

            case While:
                addElement( _token, new WhileTemplateElement( segment.datum, new TemplateElements( segment.elements ) ) );
                break;

            case Foreach:
                if( segment.datum instanceof PathDatum ) {
                    PathDatum pathDatum = (PathDatum) segment.datum;
                    addElement( _token, new ForEachTemplateElement( pathDatum.getPath(), new TemplateElements( segment.elements ) ) );
                }
                else
                    logIssue( _token, "Argument for foreach() is not a path", "" );
                break;

            default:
                // it should not be possible to get here...
        }
    }


    private void handleCSS() {
        css = !css;
    }


    private void handleHTML() {
        html = !html;
    }


    private void handleLOG( final Token _token ) {

        String text = log.toString();
        if( text.length() > 0 )
            addElement( _token, new StringTemplateElement( text ) );
    }


    private void handleElse( final Token _token ) {

        // if the top of the segment stack doesn't have an IfElse on it, we've got a problem...
        Segment segment = segmentStack.peekLast();
        if( segment.type != SegmentType.IfElse ) {
            logIssue( _token, "Else associated with something other than an If", "" + segment.type );
            return;
        }

        // otherwise, swap the elements...
        List<TemplateElement> elements = segment.altElements;
        segment.altElements = segment.elements;
        segment.elements = elements;
        segment.hadElse = true;
    }


    private void handleWord( final Token _token ) {

        lastWord = (String) _token.getValue();
    }


    private void handleIntegerLiteral( final Token _token ) {

        // add our literal as an element or value...
        addElementOrValue( _token, new IntegerDatum( (Integer) _token.getValue() ) );
    }


    private void handleBooleanLiteral( final Token _token ) {

        // now add our literal as an element or value...
        addElementOrValue( _token, new BooleanDatum( (Boolean) _token.getValue() ) );
    }


    private void handleStringLiteral( final Token _token ) {

        // now add our literal as an element or value...
        addElementOrValue( _token, new StringDatum( (String) _token.getValue() ) );
    }


    private void handleComma( final Token _token ) {

        // if we don't have a preceding value on the stack, this is an error...
        if( (valueStack.size() == 0) || (valueStack.peekLast().datum == null) )
            logIssue( _token, "Unexpected comma", "," );

        lastComma = true;
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


    private void handleControls( final Token _token, final SegmentType _type ) {

        checkForEmptyValueStack( _token );

        // we need to push a new segment, and a control marker on the value stack...
        valueStack.add( new Value( null, null ) );
        segmentStack.add( new Segment( _type, Lists.newArrayList() ) );

        lastWord = null;
    }


    private void handleFunctions( final Token _token ) {

        // if the preceding word isn't a function, we've got a problem...
        FunctionDef def = FunctionDef.getByName( lastWord );
        if( def == null)
            logIssue( _token, "Invalid function name", lastWord );

        // otherwise, push the function onto the value stack...
        else {

            // if the top of stack is a datum, we'd better have a preceding comma...
            if( (valueStack.size() != 0) && (valueStack.peekLast().datum != null) && !lastComma )
                logIssue( _token, "Expected preceding comma", "," );

            valueStack.add( new Value( def, null ) );
        }

        lastWord = null;
    }


    private void handleString( final Token _token ) {

        String text = (String) _token.getValue();

        // do any stripping we need, depending on our mode...
        if( html ) text = htmlStrip( text );
        if( css  ) text = cssStrip( text );

        if( text.length() > 0 )
            addElement( _token, new StringTemplateElement( text ) );
    }


    private void handleCloseParen( final Token _token ) {

        // if there are no values on the stack, then we have a bad situation (too many close parens)...
        if( valueStack.size() == 0 ) {
            logIssue( _token, "Unexpected close parentheses", ")" );
            return;
        }

        // walk down the stack accumulating data until we hit something else...
        Deque<Datum> data = Queues.newArrayDeque();
        while( valueStack.peekLast().datum != null )
            data.addFirst( valueStack.removeLast().datum );

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
                valueStack.add( new Value( null, new BooleanDatum( false ) ) );
            }

            // otherwise, all is well and we need to push our function datum down...
            else
                valueStack.add( new Value( null, Function.create( value.type, args ) ) );

            // if the value stack now has just one entry, the value needs to be popped to become a template element...
            if( valueStack.size() == 1 ) {
                value = valueStack.removeLast();
                addElement( _token, new DatumTemplateElement( value.datum ) );
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
            segmentStack.peekLast().datum = data.getFirst();
        }
    }


    private void handleInc( final Token _token ) {

        Path lvalue = getLValue( _token );
        if( lvalue == null )
            return;

        // replace the last template element with an incrementing setter...
        insertAddSetter( lvalue, 1 );
    }


    private void handleDec( final Token _token ) {

        Path lvalue = getLValue( _token );
        if( lvalue == null )
            return;

        // replace the last template element with a decrementing setter...
        insertAddSetter( lvalue, -1 );
    }


    private void handleEqual( final Token _token ) {

        Path lvalue = getLValue( _token );
        if( lvalue == null )
            return;

        // replace the last template element with a placeholder setter...
        // the rvalue gets put into the placeholder after we process the rvalue tokens...
        // we also push a marker onto the value stack, so we know that we need to do this...
        Datum datum = new PlaceholderDatum();
        insertSetter( lvalue, datum );
        valueStack.add( new Value( null, datum ) );
    }


    private void insertAddSetter( final Path _lvalue, final int _add ) {

        // make a datum that adds the given number to the given value...
        Datum adder = Function.create( FunctionDef.add, new PathDatum( _lvalue ), new IntegerDatum( _add ) );

        // then make a setter for that...
        insertSetter( _lvalue, adder );
    }


    private void insertSetter( final Path _lvalue, final Datum _rvalue ) {

        // remove the last template element (this is safe because getLValue() checked it)...
        List<TemplateElement> elements = segmentStack.peekLast().elements;
        elements.remove( elements.size() - 1 );

        // add our new set element...
        elements.add( new SetTemplateElement( _lvalue, _rvalue ) );
    }


    /**
     * Returns the path to the lvalue in the last template element, or null if there isn't one (after logging an error).
     *
     * @param _token the token being compiled
     * @return the path to the lvalue if there is one in the the last template element
     */
    private Path getLValue( final Token _token ) {

        // make sure we're not in the middle of a function or control test...
        if( valueStack.size() > 0 ) {
            logIssue( _token, "Invalid operator in function or test", _token.toString() );
            return null;
        }

        // make sure the last template element is the right kind...
        List<TemplateElement> elements = segmentStack.peekLast().elements;
        if( (elements.size() > 0) && (elements.get( elements.size() - 1 ) instanceof DatumTemplateElement) ) {

            DatumTemplateElement element = (DatumTemplateElement) elements.get( elements.size() - 1 );
            Datum datum = element.getDatum();
            if( datum instanceof PathDatum ) {

                Path path = ((PathDatum) datum).getPath();
                if( path.isVariable() )
                    return path;
            }
        }
        logIssue( _token, "Operator requires variable on left, doesn't have one", _token.toString() );
        return null;
    }


    /**
     * Handle possible state transitions before handling the given token.  This method is invoked just before the given token is itself handled.
     *
     * @param _token the next token to be handled
     */
    private void handleStateTransitions( final Token _token ) {
        switch( _token.getType() ) {

            case Else:
            case End:
            case HTML:
            case CSS:
            case LOG:
            case String:
                ifWordMakePathElement( _token );
                checkForEmptyValueStack( _token );
                break;

            case Comma:
            case IntegerLiteral:
            case BooleanLiteral:
            case StringLiteral:
                ifWordMakeValue( _token );
                break;

            case OpenParen:
                break;

            case CloseParen:
                ifWordMakeValue( _token );
                break;

            case Word:
            case Equal:
            case Inc:
            case Dec:
                ifWordMakePathElement( _token );
                break;

            default:
        }
    }


    private void checkForEmptyValueStack( final Token _token ) {

        // if our value stack is empty, then all is well...
        if( valueStack.size() == 0 )
            return;

        // it's possible we have a placeholder and argument left; if so, clear it...
        if( valueStack.size() == 2 ) {

            Value arg = valueStack.removeLast();
            Value ph = valueStack.removeLast();
            if( (arg.datum != null) && (ph.datum instanceof PlaceholderDatum) ) {
                PlaceholderDatum pd = (PlaceholderDatum) ph.datum;
                pd.setValue( arg.datum );
                return;
            }
        }

        // otherwise, we have a problem - so log it and clear the stack, for it is bogus...
        logIssue( _token, "Expected empty value stack, but it's not", "" + valueStack.size() + "entries" );
        valueStack.clear();
    }


    private void ifWordMakePathElement( final Token _token ) {

        if( lastWord == null )
            return;

        // if the value stack isn't empty, it's an error...
        if( valueStack.size() != 0 )
            logIssue( _token, "Unexpected second word", lastWord );

        // do we have one of our built-in variables?
        if( BIV_LOG.equals( lastWord ) ) {
            addElement( _token, new DatumTemplateElement( new StringDatum( log.toString() ) ) );
            lastWord = null;
            return;
        }
        else if( BIV_LOG_NOT_EMPTY.equals( lastWord ) ) {
            addElement( _token, new DatumTemplateElement( new BooleanDatum( log.length() != 0 ) ) );
            lastWord = null;
            return;
        }

        // validate the last word as a path...
        String errs = Path.validate( lastWord );

        // if it was ok, add it as a path template element...
        if( errs == null )
            addElement( _token, new DatumTemplateElement( new PathDatum( Path.create( lastWord ) ) ) );
        else
            logIssue( _token, "Invalid path: " + errs, lastWord );

        lastWord = null;
    }


    private void ifWordMakeValue( final Token _token ) {

        if( lastWord == null )
            return;

        // do we have one of our built-in variables?
        if( BIV_LOG.equals( lastWord ) ) {
            addValue( _token, new StringDatum( log.toString() ) );
            lastWord = null;
            return;
        }
        else if( BIV_LOG_NOT_EMPTY.equals( lastWord ) ) {
            addValue( _token, new BooleanDatum( log.length() != 0 ) );
            lastWord = null;
            return;
        }

        // validate the last word as a path...
        String errs = Path.validate( lastWord );

        // if it was ok, add it as a value...
        if( errs == null )
            addValue( _token, new PathDatum( Path.create( lastWord ) ) );
        else
            logIssue( _token, "Invalid path: " + errs, lastWord );

        lastWord = null;
    }


    private void addValue( final Token _token, final Datum _datum ) {

        // if the top of stack is a datum, we'd better have a preceding comma...
        if( (valueStack.size() != 0) && (valueStack.peekLast().datum != null) && !lastComma )
            logIssue( _token, "Expected preceding comma", "," );

        valueStack.add( new Value( null, _datum ) );
    }


    private void addElementOrValue( final Token _token, final Datum _datum ) {

        if( valueStack.size() > 0 ) {

            // if the top of stack is a datum, we'd better have a preceding comma...
            if( (valueStack.size() != 0) && (valueStack.peekLast().datum != null)
                    && !(valueStack.peekLast().datum instanceof PlaceholderDatum) && !lastComma )
                logIssue( _token, "Expected preceding comma", "," );

            valueStack.add( new Value( null, _datum ) );
        }

            // otherwise, make a new template element...
        else
            addElement( _token, new DatumTemplateElement( _datum ) );

    }


    private void addElement( final Token _token, final TemplateElement _element ) {
        checkForEmptyValueStack( _token );
        segmentStack.peekLast().elements.add( _element );
    }


    /**
     * Remove all whitespace at the beginning or end of a line, except that if the last non-whitespace character on a line is not a ">", and the
     * first non-whitespace character on the following line is not a "<", then a space is left in.
     *
     * @param _text the text to strip
     * @return the stripped text
     */
    private static Pattern PASS1 = Pattern.compile( "[ \\t\\x0B\\f\\r]*\\n[ \\t\\x0B\\f\\r]*" );
    private static Pattern PASS2 = Pattern.compile( "\\> +\\<" );
    private String htmlStrip( final String _text ) {
        Matcher mat = PASS1.matcher( _text );
        String text = mat.replaceAll( "" );
        return text;
//        mat = PASS2.matcher( text );
//        return mat.replaceAll( "><" );
    }


    private String cssStrip( final String _text ) {
        // TODO: implement this...
        return _text;
    }


    public String getLog() {
        return log.toString();
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


        public Value( final FunctionDef _type, final Datum _datum ) {
            type = _type;
            datum = _datum;
        }
    }
}
