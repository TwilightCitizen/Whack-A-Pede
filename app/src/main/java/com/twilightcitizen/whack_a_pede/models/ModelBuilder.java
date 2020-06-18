/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Circle;
import com.twilightcitizen.whack_a_pede.geometry.Rectangle;
import com.twilightcitizen.whack_a_pede.geometry.Square;
import com.twilightcitizen.whack_a_pede.geometry.SquareWithHole;

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

    // Size of Circle in vertices.
    public static int sizeOfCircleInVertices( int numPoints ) { return numPoints + 2; }

    /*
    Given a Circle and a number of points about its  outer edge, generate data that specifies how to
    draw a fan of that many triangles from its center to approximate a circle.
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
            vertexData[ offset++ ] = circle.center.x + circle.radius * (float) Math.cos( angle );
            vertexData[ offset++ ] = circle.center.y + circle.radius * (float) Math.sin( angle );
            vertexData[ offset++ ] = 0.0f;
        }

        // Only a single command is needed to draw the circle as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, numVertices ) );
    }

    // Size of Circle in vertices.
    public static int sizeOfTextureCircleInVertices( int numPoints ) {
        return 1 + ( numPoints + 1 ) + numPoints * 2;
    }

    /*
    Given a Circle and a number of points about its  outer edge, generate data that specifies how to
    draw a fan of that many triangles from its center to approximate a circle.
    */
    public void appendTextureCircle( Circle circle, int numPoints ) {
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

            vertexData[ offset++ ] = circle.center.x + circle.radius * x;
            vertexData[ offset++ ] = circle.center.y + circle.radius * y;
            vertexData[ offset++ ] = 0.0f;


            vertexData[ offset++ ] = 0.5f + 0.5f * x;
            vertexData[ offset++ ] = 0.5f + 0.5f * -y;
        }

        // Only a single command is needed to draw the circle as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, numVertices ) );
    }

    // Size of Square in vertices.
    public static final int sizeOfSquareInVertices = 6;

    /*
    Given a Square, generate data that specifies how to draw a fan triangles from its center to
    compose a square.
    */
    public void appendSquare( Square square ) {
        // Find the starting vertex and the half length.
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final float halfLength = square.length / 2.0f;

        /*
        First vertex requires the 3d position of the square center, which all triangles in the
        fan will share in common.
        */
        vertexData[ offset++ ] = square.center.x;
        vertexData[ offset++ ] = square.center.y;
        vertexData[ offset++ ] = 0.0f;

        //  Subsequent vertices fan around counterclockwise from bottom left.
        vertexData[ offset++ ] = square.center.x - halfLength;
        vertexData[ offset++ ] = square.center.y - halfLength;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = square.center.x + halfLength;
        vertexData[ offset++ ] = square.center.y - halfLength;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = square.center.x + halfLength;
        vertexData[ offset++ ] = square.center.y + halfLength;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = square.center.x - halfLength;
        vertexData[ offset++ ] = square.center.y + halfLength;
        vertexData[ offset++ ] = 0.0f;

        // Repeat last outer vertex of the fan to close it.
        vertexData[ offset++ ] = square.center.x - halfLength;
        vertexData[ offset++ ] = square.center.y - halfLength;
        vertexData[ offset++ ] = 0.0f;

        // Only a single command is needed to draw the square as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, sizeOfSquareInVertices ) );
    }

    // Size of Rectangle in vertices.
    public static final int sizeOfRectangleInVertices = 6;

    /*
    Given a Rectangle, generate data that specifies how to draw a fan triangles from its center to
    compose a rectangle.
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

        //  Subsequent vertices fan around counterclockwise from bottom left.
        vertexData[ offset++ ] = rectangle.center.x - halfWidth;
        vertexData[ offset++ ] = rectangle.center.y - halfHeight;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = rectangle.center.x + halfWidth;
        vertexData[ offset++ ] = rectangle.center.y - halfHeight;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = rectangle.center.x + halfWidth;
        vertexData[ offset++ ] = rectangle.center.y + halfHeight;
        vertexData[ offset++ ] = 0.0f;

        vertexData[ offset++ ] = rectangle.center.x - halfWidth;
        vertexData[ offset++ ] = rectangle.center.y + halfHeight;
        vertexData[ offset++ ] = 0.0f;

        // Repeat last outer vertex of the fan to close it.
        vertexData[ offset++ ] = rectangle.center.x - halfWidth;
        vertexData[ offset++ ] = rectangle.center.y - halfHeight;
        vertexData[ offset++ ] = 0.0f;

        // Only a single command is needed to draw the rectangle as a triangle fan.
        drawList.add( () -> glDrawArrays( GL_TRIANGLE_FAN, startVertex, sizeOfRectangleInVertices ) );
    }

    // Size of SquareWithHole in vertices.
    public static int sizeOfSquareWithHoleInVertices( int numPointsQuarter ) {
        return ( numPointsQuarter + 2 ) * 4;
    }

    /*
    Given a SquareWithHole, generate data that specifies how to draw four fans of triangles, one
    from each corner of the square toward points circumscribing a quarter circle radiating from
    its center point to the midpoints of the corner's perpendicular sides.
    */
    public void appendSquareWithHole( SquareWithHole squareWithHole, int numPointsQuarter ) {
        // Find the starting vertex, number of vertices, and half length.
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVerticesQuarter = sizeOfSquareWithHoleInVertices( numPointsQuarter ) / 4;
        // Half length also serves as the radius for the circle hole within the square.
        final float halfLength = squareWithHole.length / 2.0f;

        // Append each corner individually.

        appendQuarterSquareWithHole(
            squareWithHole, numPointsQuarter,
            startVertex, numVerticesQuarter, 0,
            squareWithHole.center.x + halfLength,
            squareWithHole.center.y + halfLength,
            halfLength, 0.0F
        );

        appendQuarterSquareWithHole(
            squareWithHole, numPointsQuarter,
            startVertex, numVerticesQuarter, 1,
            squareWithHole.center.x + halfLength,
            squareWithHole.center.y - halfLength,
            halfLength, 0.5F
        );

        appendQuarterSquareWithHole(
            squareWithHole, numPointsQuarter,
            startVertex, numVerticesQuarter, 2,
            squareWithHole.center.x - halfLength,
            squareWithHole.center.y - halfLength,
            halfLength, 1.0F
        );

        appendQuarterSquareWithHole(
            squareWithHole, numPointsQuarter,
            startVertex, numVerticesQuarter, 3,
            squareWithHole.center.x - halfLength,
            squareWithHole.center.y + halfLength,
            halfLength, 1.5F
        );
    }

    /*
    Given a SquareWithHole, generate data that specifies how to draw a fan of triangles from a
    corner of the square toward points circumscribing a quarter circle radiating from
    its center point to the midpoints of the corner's perpendicular sides.
    */
    private void appendQuarterSquareWithHole(
        SquareWithHole squareWithHole, int numPointsQuarter,
        int startVertex, int numVerticesQuarter, int cornerOffset,
        float cornerX, float cornerY,
        float halfLength, float angleAdjustment
    ) {
        // First vertex is the corner.
        vertexData[ offset++ ] = cornerX;
        vertexData[ offset++ ] = cornerY;
        vertexData[ offset++ ] = 0.0f;

        /*
        The next set of vertices circumscribe the arc of a circle filling the square from the
        middle of an edge to the middle of an edge as determined by the angle adjustment.
        */
        for( int i = 0; i <= numPointsQuarter; i++ ) {
            /*
            Get the angle in radians that, multiplied by numPointsQuarter, circumscribes a quarter
            circle.
            */
            float angle = ( (float) i / (float) ( numPointsQuarter * 4 ) ) * ( (float) Math.PI * 2.0f );

            // Adjust the angle based on the corner the fan originates from.
            angle -= (float) ( Math.PI * angleAdjustment );

            /*
            Use trigonometry to find the X and Y coordinates of the end of the triangle's
            hypotenuse opposite its origin at the circle center cast at the given angle.
            */
            vertexData[ offset++ ] = squareWithHole.center.x + halfLength * (float) Math.cos( angle );
            vertexData[ offset++ ] = squareWithHole.center.y + halfLength * (float) Math.sin( angle );
            vertexData[ offset++ ] = 0.0f;
        }

        // Add the command to draw the bottom left quarter of the square with hole.
        drawList.add( () -> glDrawArrays(
            GL_TRIANGLE_FAN, startVertex + numVerticesQuarter * cornerOffset, numVerticesQuarter
        ) );
    }
}
