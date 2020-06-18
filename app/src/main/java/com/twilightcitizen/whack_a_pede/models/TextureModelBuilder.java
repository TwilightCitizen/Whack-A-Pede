/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Circle;

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

    // Size of Circle in vertices.
    public static int sizeOfCircleInVertices( int numPoints ) { return numPoints + 2; }

    /*
    Given a Circle and a number of points about its  outer edge, generate data that specifies how to
    draw a fan of that many triangles from its center to approximate a circle with a texture applied
    uniformly over it.
    */
    public void appendCircle( Circle circle, int numPoints ) {
        // Find the starting vertex and number of vertices.
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices( numPoints );

        /*
        First vertex requires the 3d position of the circle center, which all triangles in the
        fan will share in common.
        */
        vertexData[ offset++ ] = circle.center.x;
        vertexData[ offset++ ] = circle.center.y;
        vertexData[ offset++ ] = 0.0f;

        /*
        Texture origin 0,0 is at bottom left extending to 1,1 at top right.
        */
        vertexData[ offset++ ] = 0.5f;
        vertexData[ offset++ ] = 0.5f;

        /*
        Remaining vertices describe the positions of the other two points for each triangle,
        each being shared between two triangles in the fan.  The first one is repeated as the
        last one to close the fan.
        */
        for( int i = 0; i <= numPoints; i++ ) {
            // Get the angle in radians that, multiplied by numPoints, circumscribes the circle.
            float angle = ( (float) i / (float) numPoints ) * ( (float) Math.PI * 2.0f );

            /*
            Use trigonometry to find the X and Y coordinates of the end of the triangle's
            hypotenuse opposite its origin at the circle center cast at the given angle.
            */
            float x = (float) Math.cos( angle );
            float y =  (float) Math.sin( angle );

            // Circle center and radius specifies coordinates for its outer edge.
            vertexData[ offset++ ] = circle.center.x + circle.radius * x;
            vertexData[ offset++ ] = circle.center.y + circle.radius * y;
            vertexData[ offset++ ] = 0.0f;

            /*
            Texture center and half-width/height specify coordinates into the texture to apply.
            The Y coordinate is inverted to account for how images are stored.
            */
            vertexData[ offset++ ] = 0.5f + 0.5f * x;
            vertexData[ offset++ ] = 0.5f + 0.5f * -y;
        }

        // Only a single command is needed to draw the circle as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, numVertices ) );
    }
}