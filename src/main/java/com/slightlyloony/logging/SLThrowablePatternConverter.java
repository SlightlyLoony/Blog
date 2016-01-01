package com.slightlyloony.logging;

/*
 * Small modification of standard Apache ThrowablePatternConverter, to indent the stack trace
 */

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.util.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Outputs the Throwable portion of the LoggingEvent as a full stack trace
 * unless this converter's option is 'short', where it just outputs the first line of the trace, or if
 * the number of lines to print is explicitly specified.
 */
@Plugin(name = "SLThrowablePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "slex", "slthrowable", "slexception" })
public class SLThrowablePatternConverter extends LogEventPatternConverter {

    private String rawOption;

    /**
     * The number of lines to write.
     */
    protected final ThrowableFormatOptions options;

    /**
     * Constructor.
     * @param name Name of converter.
     * @param style CSS style for output.
     * @param options options, may be null.
     */
    protected SLThrowablePatternConverter(final String name, final String style, final String[] options) {
        super(name, style);
        this.options = ThrowableFormatOptions.newInstance(options);
        if (options != null && options.length > 0) {
            rawOption = options[0];
        }
    }

    /**
     * Gets an instance of the class.
     *
     * @param options pattern options, may be null.  If first element is "short",
     *                only the first line of the throwable will be formatted.
     * @return instance of class.
     */
    public static SLThrowablePatternConverter newInstance(final String[] options) {
        return new SLThrowablePatternConverter("Throwable", "throwable", options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder buffer) {
        final Throwable t = event.getThrown();

        if (isSubShortOption()) {
            formatSubShortOption(t, buffer);
        }
        else if (t != null && options.anyLines()) {
            formatOption(t, buffer);
        }
    }

    private boolean isSubShortOption() {
        return ThrowableFormatOptions.MESSAGE.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.LOCALIZED_MESSAGE.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.FILE_NAME.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.LINE_NUMBER.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.METHOD_NAME.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.CLASS_NAME.equalsIgnoreCase(rawOption);
    }

    private void formatSubShortOption(final Throwable t, final StringBuilder buffer) {
        StackTraceElement[] trace;
        StackTraceElement throwingMethod = null;
        int len;

        if (t != null) {
            trace = t.getStackTrace();
            if (trace !=null && trace.length > 0) {
                throwingMethod = trace[0];
            }
        }

        if (t != null && throwingMethod != null) {
            String toAppend = Strings.EMPTY;

            if (ThrowableFormatOptions.CLASS_NAME.equalsIgnoreCase(rawOption)) {
                toAppend = throwingMethod.getClassName();
            }
            else if (ThrowableFormatOptions.METHOD_NAME.equalsIgnoreCase(rawOption)) {
                toAppend = throwingMethod.getMethodName();
            }
            else if (ThrowableFormatOptions.LINE_NUMBER.equalsIgnoreCase(rawOption)) {
                toAppend = String.valueOf(throwingMethod.getLineNumber());
            }
            else if (ThrowableFormatOptions.MESSAGE.equalsIgnoreCase(rawOption)) {
                toAppend = t.getMessage();
            }
            else if (ThrowableFormatOptions.LOCALIZED_MESSAGE.equalsIgnoreCase(rawOption)) {
                toAppend = t.getLocalizedMessage();
            }
            else if (ThrowableFormatOptions.FILE_NAME.equalsIgnoreCase(rawOption)) {
                toAppend = throwingMethod.getFileName();
            }

            len = buffer.length();
            if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
                buffer.append(' ');
            }
            buffer.append(toAppend);
        }
    }

    private void formatOption(final Throwable throwable, final StringBuilder buffer) {
        final StringWriter w = new StringWriter();

        throwable.printStackTrace(new PrintWriter(w));
        final int len = buffer.length();
        if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
            buffer.append(' ');
        }
        if (!options.allLines() || !Constants.LINE_SEPARATOR.equals(options.getSeparator())) {
            final StringBuilder sb = new StringBuilder();
            final String[] array = w.toString().split(Constants.LINE_SEPARATOR);
            final int limit = options.minLines(array.length) - 1;
            sb.append( "    " );
            for (int i = 0; i <= limit; ++i) {
                sb.append( "    " );
                sb.append(array[i]);
                if (i < limit) {
                    sb.append(options.getSeparator());
                }
            }
            buffer.append(sb.toString());

        } else {
            buffer.append( "    " );
            String trace = w.toString().replaceAll( "\n", "\n    " );
            buffer.append( trace.substring( 0, trace.length() - 4 ) );
        }
    }

    /**
     * This converter obviously handles throwables.
     *
     * @return true.
     */
    @Override
    public boolean handlesThrowable() {
        return true;
    }
}