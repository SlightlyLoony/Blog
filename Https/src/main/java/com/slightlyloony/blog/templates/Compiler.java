package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.templates.compiler.Tokenizer;
import com.slightlyloony.blog.templates.compiler.tokens.Token;

import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Compiler {


    public Template compile( final String _source ) {

        // first we tokenize it...
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize( _source );

        // if there were no errors, then we do the actual compilation...
        String tokenizingErrors = tokenizer.getLog();
        if( "".equals( tokenizingErrors ) ) {

        }

        // if there WERE errors, we have to show them...
        else {

        }

        return null;
    }
}
