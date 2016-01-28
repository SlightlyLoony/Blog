package com.slightlyloony.blog.templates.sources;

import com.google.common.collect.Lists;
import com.slightlyloony.blog.templates.sources.data.*;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DateSource extends SourceBase implements Source {

    private static final DatumDefs DATA_DEFS = getDataDefs();


    public DateSource( final String _name, final ZonedDateTime _value ) {
        super( _name, ZonedDateTime.class, _value, DATA_DEFS );
    }


    private static DatumDefs getDataDefs() {

        List<DatumDef> result = Lists.newArrayList();

        result.add( new DatumDef( "year",        IntegerDatum.class, _source -> dt(_source).getYear() ) );
        result.add( new DatumDef( "month",       IntegerDatum.class, _source -> dt( _source ).getMonth().getValue()                                      ) );
        result.add( new DatumDef( "day",         IntegerDatum.class, _source -> dt( _source ).getDayOfMonth()                                            ) );
        result.add( new DatumDef( "hour",        IntegerDatum.class, _source -> dt( _source ).getHour()                                                  ) );
        result.add( new DatumDef( "minute",      IntegerDatum.class, _source -> dt( _source ).getMinute()                                                ) );
        result.add( new DatumDef( "second",      IntegerDatum.class, _source -> dt( _source ).getSecond()                                                ) );
        result.add( new DatumDef( "timezone",    StringDatum.class,  _source -> dt( _source ).getZone().getDisplayName( TextStyle.FULL, Locale.US  )     ) );
        result.add( new DatumDef( "month_name",  StringDatum.class,  _source -> dt( _source ).getMonth().getDisplayName( TextStyle.FULL, Locale.US )     ) );
        result.add( new DatumDef( "day_of_week", StringDatum.class,  _source -> dt( _source ).getDayOfWeek().getDisplayName( TextStyle.FULL, Locale.US ) ) );
        result.add( new DatumDef( "am_pm",       StringDatum.class,  _source -> amOrPm( dt( _source ).getHour() )                                        ) );
        result.add( new DatumDef( "hour12",      IntegerDatum.class, _source -> hour12( dt( _source ).getHour() )                                        ) );

        return new DatumDefs( result );
    }


    private static ZonedDateTime dt( final Source _source ) {
        return (ZonedDateTime) ((DateSource) _source).value;
    }


    private static String amOrPm( final int _hour ) {
        return ( _hour < 12) ? "AM" : "PM";
    }


    private static int hour12( final int _hour ) {
        return (_hour == 0) ? 12 : (_hour > 12) ? _hour - 12 : _hour;
    }


    /**
     * Returns a copy of this datum with the new given name.
     *
     * @param _name the name for the copy
     * @return the datum copy
     */
    @Override
    public Datum copy( final String _name ) {
        return new DateSource( _name, (ZonedDateTime) value );
    }
}
