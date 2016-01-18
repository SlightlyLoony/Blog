package com.slightlyloony.blog.handlers;

import java.util.Arrays;

/**
 * Represents an HTTP "Accept-Encoding" request header.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class AcceptEncodingRequestHeader {

    private final Accept[] accepts;


    public AcceptEncodingRequestHeader( final String _header ) {

        if( (_header == null) ) {
            accepts = new Accept[0];
            return;
        }

        // get our individual accept parameters...
        String[] items = _header.trim().split( " *, *" );

        // parse them all...
        accepts = new Accept[items.length];
        for( int i = 0; i < items.length; i++ )
            accepts[i] = new Accept( items[i] );

        // now sort them according to their "q" factor and specificity...
        Arrays.sort( accepts );
    }


    public Accept accept( final String _encoding ) {

        if( _encoding == null )
            return null;

        for( Accept accept : accepts ) {
            if( accept.matches( _encoding ) )
                return accept;
        }
        return null;
    }


    private class Accept implements Comparable<Accept> {

        private final String encoding;
        private final float q;
        private final boolean valid;


        private Accept( final String _spec ) {

            // we parse into temporary variables first...
            String tEncoding = "";
            float tQ = 1;
            boolean tValid = true;


            // get the encoding and any parameters...
            String[] parts = _spec.split( " *; *" );

            // it's only valid to have a single (optional) "q" parameter...
            if( parts.length > 2)
                tValid = false;

            // save our encoding...
            tEncoding = parts[0];

            // parse any q parameter we might have...
            if( parts.length == 2 ) {

                // get the name and value, and make sure we got 'em...
                String[] parameterParts = parts[1].trim().split( " *= *" );
                if( parameterParts.length != 2 )
                    tValid = false;
                else {

                    // if we've got a "q", set our quality factor...
                    if( "q".equals( parameterParts[0] ) ) {
                        try {
                            tQ = Float.parseFloat( parameterParts[1] );
                        }
                        catch( NumberFormatException e ) {
                            tValid = false;
                            tQ = 0;
                        }
                    }
                    else
                        tValid = false;
                }
            }

            // now initialize the real variables...
            encoding = tEncoding;
            q = tQ;
            valid = tValid;
        }


        public boolean matches( final String _encoding ) {
            return valid && (q > 0) && "*".equals( encoding ) || encoding.equals( _encoding );
        }


        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer as this object is less than,
         * equal to, or greater than the specified object.
         * <p>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This implies that
         * <tt>x.compareTo(y)</tt> must throw an exception iff <tt>y.compareTo(x)</tt> throws an exception.)
         * <p>
         * <p>The implementor must also ensure that the relation is transitive: <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt> implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p>
         * <p>It is strongly recommended, but <i>not</i> strictly required that <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is inconsistent with equals."
         * <p>
         * <p>In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical <i>signum</i> function,
         * which is defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i> is negative, zero
         * or positive.
         *
         * @param _other the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         */
        @Override
        public int compareTo( final Accept _other ) {

            if( !valid && !_other.valid )
                return 0;

            if( !valid )
                return 1;

            if( !_other.valid )
                return -1;

            if( q < _other.q )
                return 1;

            if( q > _other.q )
                return -1;

            if( !"*".equals( encoding ) && "*".equals( _other.encoding ) )
                return -1;

            if( "*".equals( encoding ) && !"*".equals( _other.encoding ) )
                return 1;

            return 0;
        }
    }
}
