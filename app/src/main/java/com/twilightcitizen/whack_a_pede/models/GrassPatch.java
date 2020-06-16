package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.data.VertexArray;
import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.geometry.Square;
import com.twilightcitizen.whack_a_pede.shaders.ColorShader;

/*
GrassPatch is a model that specifies a patch of grass on the Lawn built up from merely a Square
using a Model Builder.  It requires the length of the square it is built from, and it exposes
methods to bind its data to an OpenGL program and execute the drawing commands that OpenGL can
use to draw it with that program.  OpenGL ES 2.0 for Android by Kevin Brothaler inspired the design
of this class, and it includes some functionality that was factored out of ModelBuilder.  It
provides  primitives to build up a model from geometry at the moment, but it should not know how a
GrassPatch or any other game model actually describes itself in such terms.
*/
public class GrassPatch {
    // Vertices of a GrassPatch carry only the X, Y, and Z position in a 3d cartesian coordinate space.
    private static final int POSITION_COMPONENT_COUNT = 3;

    // Data generated from the builder used to compose the GrassPatch.
    private ModelBuilder.GeneratedData generatedData;

    // Upon creation, just build up the GrassPatch from an appropriately specified Square.
    public GrassPatch( float length ) {
        ModelBuilder builder = new ModelBuilder( ModelBuilder.sizeOfSquareInVertices );

        builder.appendSquare( new Square( new Point( 0.0f, 0.0f ), length ) );

        generatedData = builder.build();
    }

    // Associate the vertices in the Generated Data with a ColorShader program to draw them.
    public void bindData( ColorShader colorProgram ) {
        // Convert them to a VertexArray on the graphics hardware first.
        VertexArray vertexArray = new VertexArray( generatedData.vertexData );

        // Then let OpenGL know through which location in the ColorShader it should feed.
        vertexArray.setVertexAttributePointer(
            0, colorProgram.getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT, 0
        );
    }

    // Execute all the drawing commands in the Generated Data to actually draw it.
    public void draw() {
        for( ModelBuilder.DrawCommand drawCommand : generatedData.drawList ) drawCommand.draw();
    }
}
