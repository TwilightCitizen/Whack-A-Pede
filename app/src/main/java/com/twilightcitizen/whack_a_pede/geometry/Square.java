/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.geometry;

/*
Square describes the bare minimum information necessary for a square to be constructed from OpenGL
primitives.  The center fixes it to a position in three-dimensional cartesian coordinate space, and
the length expresses the length of any of its 4 edges.
*/
public class Square {
    // The center of the square positioned in 3D cartesian coordinate space.
    public final Point center;
    // The length of any of the square's 4 sides.
    public final float length;

    // Set the square center and length at creation.
    public Square( Point center, float length ) { this.center = center; this.length = length; }
}
