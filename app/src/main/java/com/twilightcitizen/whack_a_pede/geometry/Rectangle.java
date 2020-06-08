/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.geometry;

/*
Rectangle describes the bare minimum information necessary for a rectangle to be constructed from
OpenGL primitives.  The center fixes it to a position in three-dimensional cartesian coordinate
space, and the height and width express the length of its sides and top/bottom.
*/
public class Rectangle {
    // The center of the rectangle positioned in 3D cartesian coordinate space.
    public final Point center;
    // The length of the rectangle sides and top/bottom.
    public final float height, width;

    // Set the square center and length at creation.
    public Rectangle( Point center, float height, float width ) {
        this.center = center; this.height = height; this.width = width;
    }
}