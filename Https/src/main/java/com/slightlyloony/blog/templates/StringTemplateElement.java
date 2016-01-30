package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;
import com.slightlyloony.blog.util.Constants;

/**
 * Provides a template element for a fixed string.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class StringTemplateElement implements TemplateElement {

    private final byte[] text;
    private int index;


    public StringTemplateElement( final String _text ) {

        if( (_text == null) || (_text.length() == 0) )
            throw new HandlerIllegalArgumentException( "Argument is missing or empty" );

        text = _text.getBytes( Constants.UTF8 );
        reset();
    }


    /**
     * Resets the template element in preparation for template rendering.
     */
    @Override
    public void reset() {
        index = 0;
    }


    /**
     * Reads the next byte of data from the template element. The value byte is returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the template has been reached, the value <code>-1</code> is returned.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the template element is reached.
     */
    @Override
    public int read() {

        return (index >= text.length) ? -1 : 0xFF & text[index++];
    }


    /**
     * Reads some number of bytes from the template element and stores them into the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer.
     * <p>
     * <p> If the length of <code>b</code> is zero, then no bytes are read and <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the template element is at the end of its data, the value <code>-1</code> is returned;
     * otherwise, at least one byte is read and stored into <code>b</code>.
     * <p>
     * <p> The first byte read is stored into element <code>b[0]</code>, the next one into <code>b[1]</code>, and so on. The number of bytes read is,
     * at most, equal to the length of <code>b</code>. Let <i>k</i> be the number of bytes actually read; these bytes will be stored in elements
     * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>, leaving elements <code>b[</code><i>k</i><code>]</code> through
     * <code>b[b.length-1]</code> unaffected.
     * <p>
     * <p> The <code>read(b)</code> method  has the same effect as: <pre><code> read(b, 0, b.length) </code></pre>
     *
     * @param _bytes the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the template element has
     * been reached.
     */
    @Override
    public int read( final byte[] _bytes ) {

        if( _bytes == null )
            throw new HandlerIllegalArgumentException( "Missing bytes argument" );

        return read( _bytes, 0, _bytes.length );
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

        if( _bytes == null )
            throw new HandlerIllegalArgumentException( "Missing bytes argument" );

        if( (_off < 0) || (_off >= _bytes.length) || (_len < 0) || (_off + _len > _bytes.length) )
            throw new HandlerIllegalArgumentException( "Offset or length is out of range" );

        if( _len == 0 )
            return 0;

        if( index >= text.length )
            return -1;

        int actLen = Math.min( _len, text.length - index );
        System.arraycopy( text, index, _bytes, _off, actLen  );
        index += actLen;

        return actLen;
    }
}
