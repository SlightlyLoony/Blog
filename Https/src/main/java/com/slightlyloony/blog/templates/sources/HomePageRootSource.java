package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.templates.sources.data.DataDefs;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.users.User;

import java.time.Instant;
import java.util.List;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HomePageRootSource extends RootSource {

    private static final DataDefs DATA_DEFS = getData();

    private final User user;

    /**
     * Create a new instance of this class with the given sources (or data).
     */
    protected HomePageRootSource( final User _user ) {
        super( DATA_DEFS );

        user = _user;
    }


    private static DataDefs getData() {

        List<DatumDef> sources = Lists.newArrayList();
        RootSource.addCommon( sources );
        sources.add( new DatumDef( "user", UserSource.class, _source -> ((HomePageRootSource) _source).user ) );

        return new DataDefs( sources );
    }


    // TODO: remove this test code and replace it with something real...
    public static void main( String[] args ) {

        User user = new User( "tom@dilatush.com", "slightlyloony.com", "abcd" );
        user.setCreated( Instant.now() );
        RootSource rootSource = new HomePageRootSource( user );

        String month = (String) rootSource.getValue( user, "user.created.month_name" );
        int hour = (Integer) rootSource.getValue( user, "timestamp.hour" );
        String zone = (String) rootSource.getValue( user, "user.created.timezone" );
        String ampm = (String) rootSource.getValue( user, "user.created.am_pm" );
        String dow = (String) rootSource.getValue( user, "user.created.day_of_week" );
        String handle = (String) rootSource.getValue( user, "user.handle" );
        int hour12 = (Integer) rootSource.getValue( user, "user.created.hour12" );
        Object visits = rootSource.getValue( user, "user.visits" );

        month.hashCode();
    }
}
