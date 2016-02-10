package com.slightlyloony.blog.templates;

import com.slightlyloony.blog.templates.sources.data.Datum;

/**
 * Provides a template element to handle the if ... else ... end conditional in a template.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class IfElseTemplateElement implements TemplateElement {

    private Datum test;
    private TemplateElements positive;
    private TemplateElements negative;


    public IfElseTemplateElement( final Datum _test,  final TemplateElements _posElements,  final TemplateElements _negElements  ) {

        test = _test;
        positive = _posElements;
        negative = _negElements;
    }


    /**
     * Returns a {@link TemplateInputStream} that provides the bytes in this instance in an input stream.
     *
     * @return the {@link TemplateInputStream} that provides the bytes in this instance
     */
    @Override
    public TemplateInputStream inputStream() {
        return new IfElseTemplateElementInputStream();
    }


    /**
     * Returns an estimate of the memory size of this object, in bytes.
     *
     * @return the estimated number of bytes of this object
     */
    @Override
    public int size() {
        return 24 + positive.size() + negative.size() + 50; // the 50 is just a guesstimate for the test; it's good enough for this purpose...
    }


    private class IfElseTemplateElementInputStream extends TemplateInputStream {

        private TemplateInputStream posInputStream;
        private TemplateInputStream negInputStream;
        private Boolean testResult;


        protected IfElseTemplateElementInputStream() {
            super();

            posInputStream = positive.inputStream();
            negInputStream = negative.inputStream();
            testResult = null;
        }


        private boolean test() {
            if( testResult == null )
                testResult = TemplateUtil.toBool( test.getValue() );
            return testResult;
        }


        /**
         * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to
         * <code>255</code>. If no byte is available because the end of the stream has been reached, the value <code>-1</code> is returned.
         *
         * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
         */
        @Override
        public int read() {
            return test() ? posInputStream.read() : negInputStream.read();
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
            return test() ? posInputStream.read( _bytes, _off, _len ) : negInputStream.read( _bytes, _off, _len );
        }


        /**
         * Returns the number of bytes available on the next invocation of a read method.
         *
         * @return the number of bytes available on the next invocation of a read method.
         */
        @Override
        public int available() {
            return test() ? posInputStream.available() : negInputStream.available();
        }


        /**
         * Closes this input stream and releases any memory resources associated with the stream.
         */
        @Override
        public void close() {
            if( test() )
                posInputStream.close();
            else
                negInputStream.close();
        }


        /**
         * Repositions this stream to the first position in the stream.
         */
        @Override
        public void reset() {
            if( test() )
                posInputStream.reset();
            else
                negInputStream.reset();
            testResult = null;
        }
    }

    @Override
    public String toString() {
        return "if(" + test.toString() + ")";
    }
}
