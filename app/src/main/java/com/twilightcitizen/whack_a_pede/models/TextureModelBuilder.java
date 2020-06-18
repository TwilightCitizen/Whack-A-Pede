/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Circle;
import com.twilightcitizen.whack_a_pede.geometry.Rectangle;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;

/*
TextureModelBuilder is a derivative ModelBuilder for building models to be shaded with textures.
This is heavily adapted from OpenGL ES 2.0 for Android by Kevin Brothaler for more succinctness
and better performance.
*/
public class TextureModelBuilder extends ModelBuilder {
    /*
    There are 5 floats needed for each texture vertex for the X, Y, Z, S, and T cartesian coordinate
    space components.  Textures and colors are applied externally and uniformly.
    */
    private static final int FLOATS_PER_VERTEX = 5;

    // At creation, initialize vertex data with as large enough float array.
    public TextureModelBuilder( int sizeInVertices ) { super( sizeInVertices * FLOATS_PER_VERTEX ); }

    // Size of Rectangle in vertices.
    public static final int sizeOfRectangleInVertices = 6;

    /*
    Given a Rectangle, generate data that specifies how to draw a fan triangles from its center to
    compose a rectangle with a texture applied uniformly over it.
    */
    public void appendRectangle( Rectangle rectangle ) {
        // Find the starting vertex adn the half height and width.
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final float halfHeight = rectangle.height / 2.0f;
        final float halfWidth = rectangle.width / 2.0f;

        /*
        First vertex requires the 3d position of the square center, which all triangles in the
        fan will share in common.
        */
        vertexData[ offset++ ] = rectangle.center.x;
        vertexData[ offset++ ] = rectangle.center.y;
        vertexData[ offset++ ] = 0.0f;
        /*
        Texture coordinates center.  Note that texture coordinates S and T originate at bottom left
        of 0,0 through top right of 1,1.
        */
        vertexData[ offset++ ] = 0.5f;
        vertexData[ offset++ ] = 0.5f;

        /*
        Subsequent vertices fan around counterclockwise from bottom left.  Note that texture
        coordinate T is inverted due to how images are stored.
        */
        vertexData[ offset++ ] = rectangle.center.x - halfWidth;
        vertexData[ offset++ ] = rectangle.center.y - halfHeight;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 1.0f;

        vertexData[ offset++ ] = rectangle.center.x + halfWidth;
        vertexData[ offset++ ] = rectangle.center.y - halfHeight;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 1.0f;
        vertexData[ offset++ ] = 1.0f;

        vertexData[ offset++ ] = rectangle.center.x + halfWidth;
        vertexData[ offset++ ] = rectangle.center.y + halfHeight;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 1.0f;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = rectangle.center.x - halfWidth;
        vertexData[ offset++ ] = rectangle.center.y + halfHeight;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 0.0f;

        // Repeat last outer vertex of the fan to close it.
        vertexData[ offset++ ] = rectangle.center.x - halfWidth;
        vertexData[ offset++ ] = rectangle.center.y - halfHeight;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 0.0f;
        vertexData[ offset++ ] = 1.0f;

        // Only a single command is needed to draw the rectangle as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, sizeOfRectangleInVertices ) );
    }
}