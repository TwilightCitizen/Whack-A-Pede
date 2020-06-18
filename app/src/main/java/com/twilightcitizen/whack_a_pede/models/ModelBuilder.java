/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import java.util.ArrayList;
import java.util.List;

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

    // The vertex data to be packaged up in the generated data.
    protected final float[] vertexData;

    // The drawing commands to be packaged up in the generated data.
    protected final List< DrawCommand > drawList = new ArrayList<>();

    // Always start at the beginning of the vertex data array.
    protected int offset = 0;

    // At creation, initialize vertex data with as large enough float array.
    protected ModelBuilder( int sizeInVertices ) { vertexData = new float[ sizeInVertices ]; }

    // When the ModelBuilder is built, it returns its Generated Data.
    public GeneratedData build() { return new GeneratedData( vertexData, drawList ); }

    // Size of Circle in vertices.
    public static int sizeOfCircleInVertices( int numPoints ) { return numPoints + 2; }
}
