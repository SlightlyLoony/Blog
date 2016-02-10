package com.slightlyloony.blog.templates;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A list of template elements that act together as a single template element.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class TemplateElements implements TemplateElement {

    private final ImmutableList<TemplateElement> elements;


    public TemplateElements( final List<TemplateElement> _elements ) {
        elements = ImmutableList.copyOf( _elements );
    }


    /**
     * Returns a {@link TemplateInputStream} that provides the bytes in this instance in an input stream.
     *
     * @return the {@link TemplateInputStream} that provides the bytes in this instance
     */
    @Override
    public TemplateInputStream inputStream() {
        return new TemplateElementsInputStream();
    }


    /**
     * Returns an estimate of the memory size of this object, in bytes.
     *
     * @return the estimated number of bytes of this object
     */
    @Override
    public int size() {
        int size = 8 + (elements.size() * 8 );
        for( TemplateElement element : elements )
            size += element.size();
        return size;
    }


    private class TemplateElementsInputStream extends TemplateInputStream {

        private TemplateInputStream[] inputStreams;
        private int index;


        protected TemplateElementsInputStream() {
            super();

            index = 0;

            // make an array of input streams, one for each of the template elements our parent instance has...
            inputStreams = new TemplateInputStream[ elements.size() ];
            for( int i = 0; i < elements.size(); i++ )
                inputStreams[i] = elements.get( i ).inputStream();
        }


        /**
         * Reads the next byte of data from the input stream. The value byte is returned as an <code>int</code> in the range <code>0</code> to
         * <code>255</code>. If no byte is available because the end of the stream has been reached, the value <code>-1</code> is returned.
         *
         * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
         */
        @Override
        public int read() {

            // loop through the input streams until either (a) we get a byte, or (b) we run out of input streams to read from...
            int byteRead = -1;
            while( (byteRead < 0) && (index < inputStreams.length ) ) {

                byteRead = inputStreams[index].read();
                if( byteRead < 0 )
                    index++;
            }
            return byteRead;
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

            int totalBytesRead = 0;
            int offsetIntoBuffer = _off;
            int remainingBytesInBuffer = _len;
            boolean done = false;

            // "done" means that we've read as much as possible, up to the full length of the buffer we got...
            while( !done ) {

                // if we ran out of input streams to read from, we're done...
                if( index >= inputStreams.length )
                    done = true;

                else {
                    // try to read up to our entire remaining buffer from the current input stream we're reading from...
                    int bytesRead = inputStreams[index].read( _bytes, offsetIntoBuffer, remainingBytesInBuffer );

                    // if we didn't read any bytes at all, it's time to try the next input stream (if there is one)...
                    if( bytesRead < 0 )
                        index++;

                    else {

                        // we read some bytes - yay! - so update all our counters to reflect what happened...
                        offsetIntoBuffer += bytesRead;
                        remainingBytesInBuffer -= bytesRead;
                        totalBytesRead += bytesRead;

                        // if there's no more space to read bytes into, then we're done...
                        if( remainingBytesInBuffer < 1 )
                            done = true;
                    }
                }
            }

            // if somehow we read no bytes, then we're at the end...
            if( totalBytesRead == 0 )
                totalBytesRead = -1;

            return totalBytesRead;
        }


        /**
         * Returns the number of bytes available on the next invocation of a read method.
         *
         * @return the number of bytes available on the next invocation of a read method.
         */
        @Override
        public int available() {

            int sum = 0;
            for( int i = index; i < inputStreams.length; i++ )
                sum += inputStreams[index].available();

            return sum;
        }


        /**
         * Closes this input stream and releases any memory resources associated with the stream.
         */
        @Override
        public void close() {
            index = 0;
            for( TemplateInputStream is : inputStreams )
                is.close();
        }


        /**
         * Repositions this stream to the first position in the stream.
         */
        @Override
        public void reset() {
            index = 0;
            for( TemplateInputStream is : inputStreams )
                is.reset();
        }
    }
}
