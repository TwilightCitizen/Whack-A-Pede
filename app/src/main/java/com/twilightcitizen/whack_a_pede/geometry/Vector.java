package com.twilightcitizen.whack_a_pede.geometry;

import java.util.Objects;

/*
Vector represents the direction and magnitude of something through three-dimensional space in terms
of a cartesian coordinate system. X, Y, and Z ordinarily correspond axes along the width, height,
and depth of that space.  NOTE: Z is assumed as 0.0f.
*/
public class Vector {
    // The X, Y, and Z cartesian space coordinates.
    public final float x, y;

    // Set the X, Y, and Z cartesian space coordinates at creation.
    public Vector( float x, float y ) {
        this.x = x; this.y = y;
    }

    // Provide equality testing for vectors.
    @Override public boolean equals( Object o ) {
        // Vector always equals itself.
        if( this == o ) return true;

        // Vector never equals a non-vector.
        if( o == null || getClass() != o.getClass() ) return false;

        Vector vector = (Vector) o;

        // Equal vectors share equivalent X and Y components.
        return Float.compare( vector.x, x ) == 0 && Float.compare( vector.y, y ) == 0;
    }

    @Override public int hashCode() { return Objects.hash( x, y ); }

    // Static vector instances for useful directions.
    public static final Vector up = new Vector( 0.0f, 1.0f );
    public static final Vector down = new Vector( 0.0f, -1.0f );
    public static final Vector left = new Vector( -1.0f, 0.0f );
    public static final Vector right = new Vector( 1.0f, 0.0f );

    // Get the target rotation for a model oriented in this direction.
    public float getTargetRotation() {
        if( this == Vector.down ) return 180.0f;
        else if( this ==  Vector.left ) return  90.0f;
        else if( this ==  Vector.right ) return  270.0f;
        else return 0.0f;
    }
}