/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.data.VertexArray;
import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.geometry.Rectangle;
import com.twilightcitizen.whack_a_pede.shaders.TextureShader;

/*
Lawn  is a model that specifies the entire region of the Lawn built up from merely a Rectangle
using a Model Builder.  It requires the height and width of the rectangle it is built from, and it
exposes methods to bind its data to an OpenGL program and execute the drawing commands that OpenGL
can use to draw it with that program.  OpenGL ES 2.0 for Android by Kevin Brothaler inspired the
design of this class, and it includes some functionality that was factored out of ModelBuilder.  It
provides primitives to build up a model from geometry at the moment, but it should not know how a
Lawn or any other game model actually describes itself in such terms.  NOTE: Z is assumed as 0.0f.
*/
public class Lawn {
    // Vertices of a lawn carry only the X, Y, and Z position in a 3d cartesian coordinate space.
    private static final int POSITION_COMPONENT_COUNT = 3;
    // Vertices of a 2D texture carry only S and T coordinates.
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;

    // Byte count per float needed for stride.
    public static final int BYTES_PER_FLOAT = 4;

    // Stride allows OpenGL to read segment and texture vertex coordinates from the same float buffer.
    private static final int STRIDE =
        ( POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT ) * BYTES_PER_FLOAT;

    // Data generated from the builder used to compose the lawn.
    private final TextureModelBuilder.GeneratedData generatedData;

    // Upon creation, just build up the lawn from an appropriately specified rectangle.
    public Lawn( float height, float width ) {
        TextureModelBuilder builder = new TextureModelBuilder(
            TextureModelBuilder.sizeOfRectangleInVertices
        );

        builder.appendRectangle( new Rectangle( new Point( 0.0f, 0.0f ), height, width ) );

        generatedData = builder.build();
    }

    // Associate the vertices in the Generated Data with a TextureShader program to draw them.
    public void bindData( TextureShader textureShader ) {
        // Convert them to a VertexArray on the graphics hardware first.
        VertexArray vertexArray = new VertexArray( generatedData.vertexData );

        // Let OpenGL know from which location in the TextureShader it should read segment vertices.
        vertexArray.setVertexAttributePointer(
            0, textureShader.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT, STRIDE
        );

        // Let OpenGL know from which location in the TextureShader it should read texture vertices.
        vertexArray.setVertexAttributePointer(
            POSITION_COMPONENT_COUNT,
            textureShader.getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE
        );
    }

    // Execute all the drawing commands in the Generated Data to actually draw it.
    public void draw() {
        for( ModelBuilder.DrawCommand drawCommand : generatedData.drawList ) drawCommand.draw();
    }
}