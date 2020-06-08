/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.geometry;

/*
SquareWithHole describes the bare minimum information necessary for a square with a circle hole cut
out of its center to be constructed from OpenGL primitives.  The center fixes it to a position in
three-dimensional cartesian coordinate space, and the length expresses the length of any of its 4
edges.  The radius of the circle hole cut out of it will extend from the center to the center of
any of its edges.
*/
public class SquareWithHole {
    // The center of the square with a hole in it positioned in 3D cartesian coordinate space.
    public final Point center;
    // The length of any of the square's 4 sides.
    public final float length;

    // Set the square with a hole center and length at creation.
    public SquareWithHole( Point center, float length ) {
        this.center = center; this.length = length;
    }
}
