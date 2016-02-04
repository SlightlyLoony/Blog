package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.templates.*;
import com.slightlyloony.blog.templates.functions.Function;
import com.slightlyloony.blog.templates.functions.FunctionDef;
import com.slightlyloony.blog.templates.sources.data.*;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.util.S;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HomePageRootSource extends RootSource {

    /**
     * Create a new instance of this class with the given sources (or data).
     */
    protected HomePageRootSource( final User _user, final List<Source> _sources ) {
        super( getData( _user, _sources ) );
    }


    private static DatumDefs getData( final User _user, final List<Source> _sources ) {

        List<DatumDef> sources = Lists.newArrayList();
        RootSource.addCommon( sources );
        sources.add( new DatumDef( "user", UserSource.class, _source -> _user ) );
        sources.add( new DatumDef( "users", ListSource.class, _source -> _sources ) );
        sources.add( new DatumDef( "", VariableSource.class, _source -> null ) );

        return new DatumDefs( sources );
    }


    // TODO: remove this test code and replace it with something real...
    public static void main( String[] args ) throws IOException {

        int a = 0;

        User user = new User( "tom@dilatush.com", "slightlyloony.com", "abcd" );
        user.setFirstName( "Tom" );
        user.setLastName( "Dilatush" );
        user.setCreated( Instant.now() );
        user.getRights().add( BlogAccessRight.MANAGER );
        user.getRights().add( BlogAccessRight.AUTHOR );
        user.getRights().add( BlogAccessRight.REVIEWER );

        User user2 = new User( "debi@dilatush.com", "slightlyloony.com", "dfss" );
        user2.setFirstName( "Debbie" );
        user2.setLastName( "Dilatush" );
        user2.setCreated( Instant.now() );
        user2.getRights().add( BlogAccessRight.ADULT );
        List<Source> sources = Lists.newArrayList( new UserSource( user ), new UserSource( user2 ) );

        RootSource rootSource = new HomePageRootSource( user, sources );

        TemplateRenderingContext.set( rootSource, user );

        TemplateElements ifTrue = new TemplateElements( Lists.newArrayList( new StringTemplateElement( "It's true!  " ) ) );
        TemplateElements ifFalse = new TemplateElements( Lists.newArrayList( new StringTemplateElement( "It's a lie!  " ) ) );
        IfElseTemplateElement ifer = new IfElseTemplateElement( new BooleanDatum( false ), ifTrue, ifFalse );

        Datum whileTest = Function.create( FunctionDef.lt, new PathDatum( Path.create( ".x" ) ), new IntegerDatum( 5 ) );

        List<TemplateElement> whileElementList = Lists.newArrayList();
        whileElementList.add( new StringTemplateElement( "Testing while!\n" ) );
        whileElementList.add( new SetTemplateElement( Path.create( ".x" ), Function.create( FunctionDef.add, new PathDatum( Path.create( ".x" ) ), new IntegerDatum( 1 ) ) ) );
        TemplateElements whileElements = new TemplateElements( whileElementList );

        List<TemplateElement> forElementList = Lists.newArrayList();
        forElementList.add( new StringTemplateElement( "\nUser " ) );
        forElementList.add( new DatumTemplateElement( new PathDatum( Path.create( "users.index" ) ) ) );
        forElementList.add( new StringTemplateElement( ": " ) );
        forElementList.add( new DatumTemplateElement( new PathDatum( Path.create( "users.first_name" ) ) ) );
        forElementList.add( new StringTemplateElement( " " ) );
        forElementList.add( new DatumTemplateElement( new PathDatum( Path.create( "users.last_name" ) ) ) );
        forElementList.add( new StringTemplateElement( " is part of the machine...  (" ) );
        forElementList.add( new DatumTemplateElement( Function.create( FunctionDef.odd, new PathDatum( Path.create( "users.index" ) ) ) ) );
        forElementList.add( new StringTemplateElement( ")" ) );
        TemplateElements forElements = new TemplateElements( forElementList );

        List<TemplateElement> baseElements = Lists.newArrayList();
        baseElements.add( new StringTemplateElement( "This is the beginning of a test.  We want to see if " ) );
        baseElements.add( new DatumTemplateElement( new PathDatum( Path.create( "user.first_name" ) ) ) );
        baseElements.add( new StringTemplateElement( " has any idea what he's doing!  " ) );
        baseElements.add( ifer );
        Function toUpperFirstName = Function.create( FunctionDef.upper, new PathDatum( Path.create( "user.first_name" ) ) );
        Function toUpperLastName = Function.create( FunctionDef.upper, new PathDatum( Path.create( "user.last_name" ) ) );
        Function firstInitial = Function.create( FunctionDef.left, toUpperFirstName, new IntegerDatum( 1 ) );
        Function lastInitial = Function.create( FunctionDef.left, toUpperLastName, new IntegerDatum( 1 ) );
        Function initials = Function.create( FunctionDef.concat, firstInitial, lastInitial );
        baseElements.add( new DatumTemplateElement( initials ) );
        baseElements.add( new SetTemplateElement( Path.create( ".x" ), new IntegerDatum( 0 ) ) );
        baseElements.add( new WhileTemplateElement( whileTest, whileElements ) );
        baseElements.add( new ForEachTemplateElement( Path.create( "users" ), forElements ) );
        TemplateElements base = new TemplateElements( baseElements );

        Template template = new Template( base );

        String result = S.fromUTF8( ByteStreams.toByteArray( template.inputStream() ) );
        result.hashCode();
    }
}
