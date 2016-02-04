package com.slightlyloony.blog.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP "Accept" request header.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class AcceptRequestHeader {

    private final Accept[] accepts;


    public AcceptRequestHeader( final String _header ) {

        if( (_header == null) || (_header.trim().length() == 0) ) {
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


    public Accept accept( final String _mediaType ) {

        if( _mediaType == null )
            return null;

        String[] parts = _mediaType.split( " */ *" );
        if( parts.length != 2 )
            return null;

        for( Accept accept : accepts ) {
            if( accept.matches( parts[0], parts[1] ) )
                return accept;
        }
        return null;
    }


    public class Accept implements Comparable<Accept> {

        private final String type;
        private final String subType;
        private final Map<String,String> parameters;
        private final float q;
        private final boolean valid;


        private Accept( final String _spec ) {

            // we parse into temporary variables first...
            String tType = "";
            String tSubType = "";
            Map<String,String> tParameters = null;
            float tQ = 1;
            boolean tValid = true;


            // get the media range and any parameters...
            String[] parts = _spec.split( " *; *" );

            // get the media range and subrange; make sure we got 'em...
            String[] rangeParts = parts[0].split( " */ *" );
            if( rangeParts.length != 2 ) {
                tValid = false;
                tType = parts[0];
                tSubType = "";
            }
            else {
                tType = rangeParts[0];
                tSubType = rangeParts[1];
            }

            // parse any parameters we have...
            for( int i = 1; i < parts.length; i++ ) {

                // get the name and value, and make sure we got 'em...
                String[] parameterParts = parts[i].trim().split( " *= *" );
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

                    // otherwise, process the special parameter...
                    else {

                        if( tParameters == null )
                            tParameters = new HashMap<>();
                        tParameters.put( parameterParts[0], parameterParts[1] );
                    }
                }
            }

            // now initialize the real variables...
            type = tType;
            subType = tSubType;
            parameters = tParameters;
            q = tQ;
            valid = tValid;
        }


        public boolean matches( final String _type, final String _subType ) {
            return valid && ("*".equals( type ) || (type.equals( _type ) && ("*".equals( subType ) || subType.equals( _subType ))));
        }


        public String get( final String _parameter ) {
            return (parameters == null) ? null : parameters.get( _parameter );
        }


        /**
         * Compares this object with the specified object for order.  Returns a negative integer, zero, or a positive integer as this object is less than,
         * equal to, or greater than the specified object.
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) == -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This implies that
         * <tt>x.compareTo(y)</tt> must throw an exception iff <tt>y.compareTo(x)</tt> throws an exception.)
         * </p>
         * <p>The implementor must also ensure that the relation is transitive: <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * </p>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt> implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * </p>
         * <p>It is strongly recommended, but <i>not</i> strictly required that <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is inconsistent with equals."
         * </p>
         * <p>In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical <i>signum</i> function,
         * which is defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i> is negative, zero
         * or positive.</p>
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

            if( !"*".equals( type ) && "*".equals( _other.type ) )
                return -1;

            if( "*".equals( type ) && !"*".equals( _other.type ) )
                return 1;

            if( !"*".equals( subType ) && "*".equals( _other.subType ) )
                return -1;

            if( "*".equals( subType ) && !"*".equals( _other.subType ) )
                return 1;

            return 0;
        }
    }
}
