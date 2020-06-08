/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Circle;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;

/*
ModelBuilder supports the creation of three-dimensional models of things, built up or composed from
geometry, that can be returned as generated data that describes all the vertices of the model and
distinct drawing commands OpenGL will need to draw them appropriately.  This is heavily adapted from
OpenGL ES 2.0 for Android by Kevin Brothaler for more succinctness and better performance.
*/
public class ModelBuilder {
    // A drawing command to execute.
    interface DrawCommand { void draw(); }

    // Generated data returned from models that build themselves up from geometry, which includes
    // the vertices that describe the geometry and the commands OpenGL should use to draw it.
    static class GeneratedData {
        // Vertex data for the composed geometry.
        final float[] vertexData;
        // Drawing commands OpenGL needs to draw it appropriately.
        final List< DrawCommand > drawList;

        // Just package vertex data and drawing commands together at creation.
        GeneratedData( float[] vertexData, List< DrawCommand > drawList ) {
            this.vertexData = vertexData; this.drawList = drawList;
        }
    }

    /*
    There are 3 floats needed for each vertex for the X, Y, and Z cartesian coordinate space
    components.  Textures and colors are applied externally and uniformly.
    */
    private static final int FLOATS_PER_VERTEX = 3;

    // The vertex data to be packaged up in the generated data.
    private final float[] vertexData;

    // The drawing commands to be packaged up in the generated data.
    private final List< DrawCommand > drawList = new ArrayList<>();

    // Always start at the beginning of the vertex data array.
    private int offset = 0;

    // At creation, initialize vertex data with as large enough float array.
    public ModelBuilder( int sizeInVertices ) {
        vertexData = new float[ sizeInVertices * FLOATS_PER_VERTEX ];
    }

    // When the ModelBuilder is built, it returns its Generated Data.
    public GeneratedData build() { return new GeneratedData( vertexData, drawList ); }

    public static int sizeOfCircleInVertices( int numPoints ) { return numPoints + 2; }

    /*
    Given a Circle and a number of points about its  outer edge, generate data that specifies how to
    draw a fan of that many triangles from its center to approximate a circle.

    TODO: Look at pushing out of ModelBuilder into Circle somehow.
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
        vertexData[ offset++ ] = circle.center.z;

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
            hypotenuse opposite its origin at the circle center cast at the given angel.
            */
            vertexData[ offset++ ] = circle.center.x + circle.radius * (float) Math.cos( angle );
            vertexData[ offset++ ] = circle.center.y + circle.radius * (float) Math.sin( angle );
            vertexData[ offset++ ] = circle.center.z;
        }

        // Only a single command is needed to draw the circle as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, numVertices ) );
    }
}
