package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.StringDatum;
import com.slightlyloony.blog.users.User;

import java.time.Instant;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HomePageRootSource extends RootSource {

    private static final DatumDefs DATA_DEFS = getData();

    private final User user;

    /**
     * Create a new instance of this class with the given sources (or data).
     */
    protected HomePageRootSource( final User _user ) {
        super( DATA_DEFS );

        user = _user;
    }


    private static DatumDefs getData() {

        List<DatumDef> sources = Lists.newArrayList();
        RootSource.addCommon( sources );
        sources.add( new DatumDef( "user", UserSource.class, _source -> ((HomePageRootSource) _source).user ) );
        sources.add( new DatumDef( "", VariableSource.class, _source -> null ) );

        return new DatumDefs( sources );
    }


    // TODO: remove this test code and replace it with something real...
    public static void main( String[] args ) {

        User user = new User( "tom@dilatush.com", "slightlyloony.com", "abcd" );
        user.setCreated( Instant.now() );
        user.getRights().add( BlogAccessRight.MANAGER );
        user.getRights().add( BlogAccessRight.AUTHOR );
        user.getRights().add( BlogAccessRight.REVIEWER );
        RootSource rootSource = new HomePageRootSource( user );

        VariableSource vs = (VariableSource) rootSource.getDatum( user, "" );
        vs.set( "x", new StringDatum( "y", "test" ) );

        String month = (String) rootSource.getValue( user, "user.created.month_name" );
        int hour = (Integer) rootSource.getValue( user, "timestamp.hour" );
        String zone = (String) rootSource.getValue( user, "user.created.timezone" );
        String ampm = (String) rootSource.getValue( user, "user.created.am_pm" );
        String dow = (String) rootSource.getValue( user, "user.created.day_of_week" );
        String handle = (String) rootSource.getValue( user, "user.handle" );
        String rights = (String) rootSource.getValue( user, "user.rights" );
        int hour12 = (Integer) rootSource.getValue( user, "user.created.hour12" );
        Object visits = rootSource.getValue( user, "user.visits" );
        Object x = rootSource.getValue( user, ".x" );
        Object y = rootSource.getValue( user, ".y" );

        month.hashCode();
    }
}
