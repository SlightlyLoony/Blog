package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.security.BlogAccessRight;
import com.slightlyloony.blog.security.BlogUserRights;
import com.slightlyloony.blog.templates.sources.data.*;
import com.slightlyloony.blog.users.User;
import com.slightlyloony.blog.util.Defaults;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Implements a Source for user data.  Note that there may be NO user associated with a request, in which case this class must be instantiated with
 * a default class for anonymous users.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class UserSource extends SourceBase implements Source {

    private static final DatumDefs DATA_DEFS = getData();


    public UserSource( final String _name, final User _user ) {
        super( _name, User.class, _user, DATA_DEFS );
    }


    private static DatumDefs getData() {

        List<DatumDef> result = Lists.newArrayList();

        // public...
        result.add( new DatumDef( "handle",    StringDatum.class,  UserSource::getHandle    ) );
        result.add( new DatumDef( "born",      IntegerDatum.class, UserSource::getBirthYear ) );
        result.add( new DatumDef( "bio",       StringDatum.class,  UserSource::getBio       ) );
        result.add( new DatumDef( "gender",    StringDatum.class,  UserSource::getGender    ) );
        result.add( new DatumDef( "motto",     StringDatum.class,  UserSource::getMotto     ) );
        result.add( new DatumDef( "image",     StringDatum.class,  UserSource::getImage     ) );
        result.add( new DatumDef( "age",       IntegerDatum.class, UserSource::getAge       ) );
        result.add( new DatumDef( "created",   DateSource.class,   UserSource::getCreated   ) );
        result.add( new DatumDef( "blog",      StringDatum.class,  UserSource::getBlog      ) );

        // manager or user himself...
        result.add( new DatumDef( "first_name", StringDatum.class,  UserSource::getFirstName, UserSource::authManagerOrSelfOrNamePublic  ) );
        result.add( new DatumDef( "last_name",  StringDatum.class,  UserSource::getLastName,  UserSource::authManagerOrSelfOrNamePublic  ) );
        result.add( new DatumDef( "email",      StringDatum.class,  UserSource::getEmail,     UserSource::authManagerOrSelfOrEmailPublic ) );
        result.add( new DatumDef( "username",   StringDatum.class,  UserSource::getUsername,  UserSource::authManagerOrSelf              ) );

        // manager only...
        result.add( new DatumDef( "rights",          StringDatum.class,  UserSource::getRights,         UserSource::authManager ) );
        result.add( new DatumDef( "disabled",        BooleanDatum.class, UserSource::getDisabled,       UserSource::authManager ) );
        result.add( new DatumDef( "disabled_reason", StringDatum.class,  UserSource::getDisabledReason, UserSource::authManager ) );
        result.add( new DatumDef( "last_verified",   DateSource.class,   UserSource::getLastVerified,   UserSource::authManager ) );
        result.add( new DatumDef( "name_public",     BooleanDatum.class, UserSource::getNamePublic,     UserSource::authManager ) );
        result.add( new DatumDef( "email_public",    BooleanDatum.class, UserSource::getEmailPublic,    UserSource::authManager ) );
        result.add( new DatumDef( "last_visited",    DateSource.class,   UserSource::getLastVisited,    UserSource::authManager ) );
        result.add( new DatumDef( "visits",          IntegerDatum.class, UserSource::getVisits,         UserSource::authManager ) );

        return new DatumDefs( result );
    }


    private static boolean authManagerOrSelfOrNamePublic( final User _user, final Source _source ) {
        return authManager( _user, _source ) || _user.getUsername().equals( user( _source ).getUsername() ) || user( _source ).isNameIsPublic();
    }


    private static boolean authManagerOrSelfOrEmailPublic( final User _user, final Source _source ) {
        return authManager( _user, _source ) || _user.getUsername().equals( user( _source ).getUsername() ) || user( _source ).isEmailIsPublic();
    }


    private static boolean authManagerOrSelf( final User _user, final Source _source ) {
        return authManager( _user, _source ) || (_user.getUsername().equals( user( _source ).getUsername() ) );
    }


    private static boolean authManager( final User _user, final Source _source ) {
        BlogUserRights rights = _user.getRights();
        return (rights != null) && rights.has( BlogAccessRight.MANAGER );
    }


    private static String getMotto( final Source _source ) {
        return user( _source ).getMotto();
    }


    private static String getImage( final Source _source ) {
        return user( _source ).getImage();
    }


    private static Integer getAge( final Source _source ) {
        return ZonedDateTime.now().getYear() - user( _source ).getBirthYear();
    }


    private static String getBlog( final Source _source ) {
        return user( _source ).getBlog();
    }


    private static String getFirstName( final Source _source ) {
        return user( _source ).getFirstName();
    }


    private static String getLastName( final Source _source ) {
        return user( _source ).getLastName();
    }


    private static int getBirthYear( final Source _source ) {
        return user( _source ).getBirthYear();
    }


    private static String getBio( final Source _source ) {
        return user( _source ).getBio();
    }


    private static String getEmail( final Source _source ) {
        return user( _source ).getEmail();
    }


    private static String getGender( final Source _source ) {
        return user( _source ).getGender().name();
    }


    private static String getUsername( final Source _source ) {
        return user( _source ).getUsername();
    }


    private static String getRights( final Source _source ) {
        return user( _source ).getRights().toString();
    }


    private static String getDisabledReason( final Source _source ) {
        return user( _source ).getDisabledReason();
    }


    private static String getHandle( final Source _source ) {
        return user( _source ).getHandle();
    }


    private static boolean getNamePublic( final Source _source ) {
        return user( _source ).isNameIsPublic();
    }


    private static boolean getEmailPublic( final Source _source ) {
        return user( _source ).isEmailIsPublic();
    }


    private static boolean getDisabled( final Source _source ) {
        return user( _source ).isDisabled();
    }


    private static int getVisits( final Source _source ) {
        return user( _source ).getNumberOfVisits();
    }


    private static ZonedDateTime getLastVisited( final Source _source ) {
        return ZonedDateTime.ofInstant( user( _source ).getLastVisited(), Defaults.TIME_ZONE );
    }


    private static ZonedDateTime getLastVerified( final Source _source ) {
        return ZonedDateTime.ofInstant( user( _source ).getEmailLastVerified(), Defaults.TIME_ZONE );
    }


    private static ZonedDateTime getCreated( final Source _source ) {
        return ZonedDateTime.ofInstant( user( _source ).getCreated(), Defaults.TIME_ZONE );
    }


    private static User user( final Source _source ) {
        return (User) ((UserSource) _source).value;
    }


    /**
     * Returns a copy of this datum with the new given name.
     *
     * @param _name the name for the copy
     * @return the datum copy
     */
    @Override
    public Datum copy( final String _name ) {
        return new UserSource( _name, (User) value );
    }
}
