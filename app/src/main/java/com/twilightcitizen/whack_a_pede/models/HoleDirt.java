/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.data.VertexArray;
import com.twilightcitizen.whack_a_pede.geometry.Circle;
import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.shaders.ColorShader;

/*
HoleDirt is a model that specifies the dirt for a Hole in the Lawn built up from merely a Circle
using a Model Builder.  It requires the radius and number of points extending outward from its center
that describe the circle it is built from, and it exposes methods to bind its data to an OpenGL program
and execute the drawing commands that OpenGL can use to draw it with that program.  OpenGL ES 2.0 for
Android by Kevin Brothaler inspired the design of this class, and it includes some functionality that
was factored out of ModelBuilder.  ModelBuilder provides primitives to build up a model from geometry
at the moment, but it should not know how a Hole or any other game model actually describes itself in
such terms.  NOTE: Z is assumed as 0.0f.
*/
public class HoleDirt {
    // Vertices of a HoleDirt carry only the X, Y, and Z position in a 3d cartesian coordinate space.
    private static final int POSITION_COMPONENT_COUNT = 3;

    // Data generated from the builder used to compose the HoleDirt.
    private final ColorModelBuilder.GeneratedData generatedData;

    // Upon creation, just build up the HoleDirt from an appropriately specified Circle.
    public HoleDirt( float radius, int numPoints ) {
        ColorModelBuilder builder = new ColorModelBuilder(
            ColorModelBuilder.sizeOfCircleInVertices( numPoints )
        );

        builder.appendCircle( new Circle( new Point( 0.0f, 0.0f ), radius ), numPoints );

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