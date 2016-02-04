package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.handlers.HandlerIllegalArgumentException;

import java.io.InputStream;

/**
 * Base class for all template input streams; it defines all the methods required to be implemented (in addition to {@link #read()}.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class TemplateInputStream extends InputStream {


    protected TemplateInputStream() {
        super();
    }


    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream has been reached, the value <code>-1</code> is returned.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
     */
    @Override
    public abstract int read();


    /**
     * Reads some number of bytes from the template elements and stores them into the buffer array <code>_bytes</code>. The number of bytes actually
     * read is returned as an integer.
     * <p> If the length of <code>_bytes</code> is zero, then no bytes are read and <code>0</code> is returned; otherwise, there is an attempt to read
     * at least one byte. If no byte is available because the template element is at the end of its data, the value <code>-1</code> is returned;
     * otherwise, at least one byte is read and stored into <code>_bytes</code>.
     * </p>
     * <p> The first byte read is stored into element <code>_bytes[0]</code>, the next one into <code>_bytes[1]</code>, and so on. The number of bytes
     * read is, at most, equal to the length of <code>_bytes</code>. Let <i>k</i> be the number of bytes actually read; these bytes will be stored in
     * elements <code>_bytes[0]</code> through <code>_bytes[</code><i>k</i><code>-1]</code>, leaving elements <code>b[</code><i>k</i><code>]</code>
     * through <code>_bytes[_bytes.length-1]</code> unaffected.
     * </p>
     * <p> The <code>read(_bytes)</code> method  has the same effect as: <pre><code> read(_bytes, 0, _bytes.length) </code></pre>
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
     * <p> If <code>len</code> is zero, then no bytes are read and <code>0</code> is returned; otherwise, there is an attempt to read at least one
     * byte. If no byte is available because the template element is at end of its data, the value <code>-1</code> is returned; otherwise, at least
     * one byte is read and stored into <code>b</code>.
     * </p>
     * <p> The first byte read is stored into element <code>b[off]</code>, the next one into <code>b[off+1]</code>, and so on. The number of bytes
     * read is, at most, equal to <code>len</code>. Let <i>k</i> be the number of bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>, leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     * </p>
     * <p> In every case, elements <code>b[0]</code> through <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.</p>
     *
     * @param _bytes the buffer into which the data is read.
     * @param _off   the start offset in array <code>b</code> at which the data is written.
     * @param _len   the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the end of the stream has been
     * reached.
     */
    @Override
    public abstract int read( final byte[] _bytes, final int _off, final int _len );


    /**
     * Returns the number of bytes available on the next invocation of a read method.
     *
     * @return     the number of bytes available on the next invocation of a read method.
     */
    public abstract int available();


    /**
     * Closes this input stream and releases any memory resources associated with the stream.
     */
    public abstract void close();


    /**
     * Repositions this stream to the first position in the stream.
     */
    public abstract void reset();


    protected void readArgumentCheck(  final byte[] _bytes, final int _off, final int _len  ) {

        if( _bytes == null )
            throw new HandlerIllegalArgumentException( "Missing bytes argument" );

        if( (_off < 0) || (_off >= _bytes.length) || (_len < 0) || (_off + _len > _bytes.length) )
            throw new HandlerIllegalArgumentException( "Offset or length is out of range" );
    }
}
