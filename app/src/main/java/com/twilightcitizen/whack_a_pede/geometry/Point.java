/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.geometry;

import java.util.Objects;

/*
Point represents the position of something in three-dimensional space in terms of a cartesian
coordinate system. X, Y, and Z ordinarily correspond axes along the width, height, and depth of
that space.  OpenGL makes some assumptions about this space with respect to the viewport in which
it is drawn:  0 resides at the center of any of the axes, while 1 and -1 reside at the furthest
extremes.  Any values beyond 1 and -1 reside outside of the viewport and cannot be seen unless
a matrix applied to it changes the values to be within that range.  This adapts from OpenGL ES 2.0
for Android by Kevin Brothaler.  While OpenGL represents all vertex positions at points within
a three-dimensional space, two-dimensional graphics can be achieved by normalizing the third
dimension for all vertices, effectively ignoring it.  NOTE: Z is assumed as 0.0f.
*/
public class Point {
    // The X, Y, and Z cartesian space coordinates.
    public final float x, y;

    // Set the X, Y, and Z cartesian space coordinates at creation.
    public Point( float x, float y ) {
        this.x = x; this.y = y;
    }

    // Provide equality testing for points.
    @Override public boolean equals( Object o ) {
        // Point always equals itself.
        if( this == o ) return true;

        // Point never equals non-point.
        if( o == null || getClass() != o.getClass() ) return false;

        Point point = (Point) o;

        // Equal points share equivalent X and Y components.
        return Float.compare( point.x, x ) == 0 && Float.compare( point.y, y ) == 0;
    }

    @Override public int hashCode() { return Objects.hash( x, y ); }

    /*
    Test point to see if it coincides with another's X and falls between its next and previous Y.
    */
    public boolean wasPassedVertically( Point previousPosition, Point nextPosition ) {
        return
            Float.compare( x, nextPosition.x ) == 0 && ( (
                Float.compare( previousPosition.y, y ) == -1 &&
                Float.compare( y, nextPosition.y ) == -1
            ) || (
                Float.compare( previousPosition.y, y ) == 1 &&
                Float.compare( y, nextPosition.y ) == 1
            ) );
    }

    /*
    Test point to see if it coincides with another's Y and falls between its next and previous X.
    */
    public boolean wasPassedHorizontally( Point previousPosition, Point nextPosition ) {
        return
            Float.compare( y, nextPosition.y ) == 0 && ( (
                Float.compare( previousPosition.x, x ) == -1 &&
                Float.compare( x, nextPosition.x ) == -1
            ) || (
                Float.compare( previousPosition.x, x ) == 1 &&
                Float.compare( x, nextPosition.x ) == 1
            ) );
    }

    /*
    Test point to see if it intersects the line between two others somehow.
    */
    public boolean intersectsPathOf( Point previousPosition, Point nextPosition ) {
        return
            wasPassedVertically( previousPosition, nextPosition ) ||
            wasPassedHorizontally( previousPosition, nextPosition ) ||
            this.equals( nextPosition );
    }
}