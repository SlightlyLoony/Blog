package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.Blog;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.security.BlogUserRights;
import com.slightlyloony.blog.templates.TemplateRenderingContext;
import com.slightlyloony.blog.templates.sources.data.DatumDef;
import com.slightlyloony.blog.templates.sources.data.DatumDefs;
import com.slightlyloony.blog.templates.sources.data.IntegerDatum;
import com.slightlyloony.blog.templates.sources.data.StringDatum;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.users.Users;

import java.util.List;

/**
 * Implements a Source for user data.  Note that there may be NO user associated with a request, in which case this class must be instantiated with
 * a default class for anonymous users.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class BlogSource extends SourceBase implements Source {

    private static final DatumDefs DATA_DEFS = getData();


    public BlogSource( final Blog _blog ) {
        super( _blog, DATA_DEFS );
    }


    private static DatumDefs getData() {

        List<DatumDef> result = Lists.newArrayList();

        // public...
        result.add( new DatumDef( "name",        StringDatum.class,  BlogSource::getName                                ) );
        result.add( new DatumDef( "host",        StringDatum.class,  BlogSource::getHost                                ) );
        result.add( new DatumDef( "port",        IntegerDatum.class, BlogSource::getPort                                ) );
        result.add( new DatumDef( "domain",      StringDatum.class,  BlogSource::getDomain                              ) );
        result.add( new DatumDef( "displayName", StringDatum.class,  BlogSource::getDisplayName                         ) );

        // manager only...
        result.add( new DatumDef( "users",       UsersSource.class,  BlogSource::getUsers,      BlogSource::authManager ) );

        return new DatumDefs( result );
    }


    private static boolean authManager( final Source _source ) {
        User user = TemplateRenderingContext.get().getUser();
        BlogUserRights rights = user.getRights();
        return (rights != null) && rights.has( BlogAccessRight.MANAGER );
    }


    private static String getName( final Source _source ) {
        return blog( _source ).getName();
    }


    private static String getHost( final Source _source ) {
        return blog( _source ).getHost();
    }


    private static Integer getPort( final Source _source ) {
        return blog( _source ).getConfig().getPort();
    }


    private static String getDomain( final Source _source ) {
        return blog( _source ).getConfig().getDomain();
    }


    private static String getDisplayName( final Source _source ) {
        return blog( _source ).getConfig().getDisplayName();
    }


    private static Users getUsers( final Source _source ) {
        return blog( _source ).getUsers();
}


    private static Blog blog( final Source _source ) {
        return (Blog) ((BlogSource) _source).value;
    }
}
