package com.twilightcitizen.whack_a_pede.geometry;

import java.util.Objects;

/*
Vector represents the direction and magnitude of something through three-dimensional space in terms
of a cartesian coordinate system. X, Y, and Z ordinarily correspond axes along the width, height,
and depth of that space.
*/
public class Vector {
    // The X, Y, and Z cartesian space coordinates.
    public final float x, y;

    // Set the X, Y, and Z cartesian space coordinates at creation.
    public Vector( float x, float y ) {
        this.x = x; this.y = y;
    }

    @Override public boolean equals( Object o ) {
        if( this == o ) return true;

        if( o == null || getClass() != o.getClass() ) return false;

        Vector vector = (Vector) o;

        return Float.compare( vector.x, x ) == 0 &&
               Float.compare( vector.y, y ) == 0;
    }

    @Override public int hashCode() { return Objects.hash( x, y ); }

    // Static vector instances for useful directions.
    public static final Vector up = new Vector( 0.0f, 1.0f );
    public static final Vector down = new Vector( 0.0f, -1.0f );
    public static final Vector left = new Vector( -1.0f, 0.0f );
    public static final Vector right = new Vector( 1.0f, 0.0f );
}