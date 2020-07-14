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

import static com.twilightcitizen.whack_a_pede.models.ModelBuilder.DrawCommand;
import static com.twilightcitizen.whack_a_pede.models.ModelBuilder.GeneratedData;

/*
PowerUpRendering is a model that specifies a power up built up from merely a Rectangle using a Model
Builder.  It requires the height and width describing the rectangle it is built from, and it exposes
methods to bind its data to an OpenGL program and execute the drawing commands that OpenGL can use
to draw it with that program.  This may seem overkill considering that PowerUpRendering just wraps a Rectangle,
but this can be extended later to be built up from additional shapes such as legs, or even use the third
dimension (depth) and specify Segment as some other combination of geometry.  OpenGL ES 2.0 for Android
by Kevin Brothaler inspired the design of this class, and it includes some functionality that was factored
out of ModelBuilder.  ModelBuilder provides  primitives to build up a model from geometry at the moment,
but it should not know how a Segment or any other game model actually describes itself in such terms.
NOTE: Z is assumed as 0.0f.
*/
public class PowerUpRendering {
    // Vertices of a PowerUpRendering carry only the X, Y, and Z position in a 3d cartesian coordinate space.
    private static final int POSITION_COMPONENT_COUNT = 3;
    // Vertices of a 2D texture carry only S and T coordinates.
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;

    // Byte count per float needed for stride.
    public static final int BYTES_PER_FLOAT = 4;

    // Stride allows OpenGL to read power and texture vertex coordinates from the same float buffer.
    private static final int STRIDE =
        ( POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT ) * BYTES_PER_FLOAT;

    // Data generated from the builder used to compose the PowerUp.
    private final GeneratedData generatedData;

    // Upon creation, just build up the PowerUpRendering from an appropriately specified Rectangle.
    public PowerUpRendering( float height, float width ) {
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
        for( DrawCommand drawCommand : generatedData.drawList ) drawCommand.draw();
    }
}