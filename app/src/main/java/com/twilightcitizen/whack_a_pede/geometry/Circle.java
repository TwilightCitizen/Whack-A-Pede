/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.geometry;

/*
Circle describes the bare minimum information necessary for a circle to be constructed from OpenGL
primitives.  The center fixes it to a position in three-dimensional cartesian coordinate space, and
the radius expresses the bounds of the outer edge as measured from that center.  This adapts from
OpenGL ES 2.0 for Android by Kevin Brothaler.
*/
public class Circle {
    // The center of the circle positioned in 3D cartesian coordinate space.
    public final Point center;
    // The distance between the outer edge of the circle and its the center.
    public final float radius;

    // Set the circle center and radius at creation.
    public Circle( Point center, float radius ) { this.center = center; this.radius = radius; }
}