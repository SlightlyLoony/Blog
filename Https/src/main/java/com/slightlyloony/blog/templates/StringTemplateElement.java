package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.util.Constants;
import com.slightlyloony.blog.util.S;

/**
 * Provides a template element for a fixed string.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StringTemplateElement implements TemplateElement {

    private final byte[] text;


    public StringTemplateElement( final String _text ) {

        if( (_text == null) || (_text.length() == 0) )
            throw new HandlerIllegalArgumentException( "Argument is missing or empty" );

        text = _text.getBytes( Constants.UTF8 );
    }


    /**
     * Returns a {@link TemplateInputStream} that provides the bytes in this instance in an input stream.
     *
     * @return the {@link TemplateInputStream} that provides the bytes in this instance
     */
    @Override
    public TemplateInputStream inputStream() {
        return new StringTemplateElementInputStream();
    }


    /**
     * Implements a {@link TemplateInputStream} that provides the bytes in this instance.
     */
    private class StringTemplateElementInputStream extends TemplateInputStream {

        private int index;

        private StringTemplateElementInputStream() {
            super();
            index = 0;
        }


        /**
         * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to
         * <code>255</code>. If no byte is available because the end of the stream has been reached, the value <code>-1</code> is returned. This method
         * blocks until input data is available, the end of the stream is detected, or an exception is thrown.
         *
         * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
         */
        @Override
        public int read() {
            return (index >= text.length) ? -1 : 0xFF & text[index++];
        }


        /**
         * Reads up to <code>len</code> bytes of data from the template element into an array of bytes.  An attempt is made to read as many as
         * <code>len</code> bytes, but a smaller number may be read. The number of bytes actually read is returned as an integer.
         * <p>
         * <p> If <code>len</code> is zero, then no bytes are read and <code>0</code> is returned; otherwise, there is an attempt to read at least one
         * byte. If no byte is available because the template element is at end of its data, the value <code>-1</code> is returned; otherwise, at least
         * one byte is read and stored into <code>b</code>.
         * <p>
         * <p> The first byte read is stored into element <code>b[off]</code>, the next one into <code>b[off+1]</code>, and so on. The number of bytes
         * read is, at most, equal to <code>len</code>. Let <i>k</i> be the number of bytes actually read; these bytes will be stored in elements
         * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>, leaving elements <code>b[off+</code><i>k</i><code>]</code> through
         * <code>b[off+len-1]</code> unaffected.
         * <p>
         * <p> In every case, elements <code>b[0]</code> through <code>b[off]</code> and elements <code>b[off+len]</code> through
         * <code>b[b.length-1]</code> are unaffected.
         *
         * @param _bytes the buffer into which the data is read.
         * @param _off   the start offset in array <code>b</code> at which the data is written.
         * @param _len   the maximum number of bytes to read.
         * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has been
         * reached.
         */
        @Override
        public int read( final byte[] _bytes, final int _off, final int _len ) {
            readArgumentCheck( _bytes, _off, _len );

            if( _len == 0 )
                return 0;

            if( index >= text.length )
                return -1;

            int actLen = Math.min( _len, text.length - index );
            System.arraycopy( text, index, _bytes, _off, actLen  );
            index += actLen;

            return actLen;
        }


        /**
         * Returns the number of bytes available on the next invocation of a read method.
         *
         * @return the number of bytes available on the next invocation of a read method.
         */
        @Override
        public int available() {
            return text.length - index;
        }


        /**
         * Closes this input stream and releases any memory resources associated with the stream.
         */
        @Override
        public void close() {
            reset();
        }


        /**
         * Repositions this stream to the first position in the stream.
         */
        @Override
        public void reset() {
            index = 0;
        }
    }


    @Override
    public String toString() {
        return "String: " + S.fromUTF8( text );
    }
}
