/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

/*
OpenGL specifies the Points, Lines, and Triangles primitives it can draw as arrays of vertex data.
Vertex data itself can be composed of many components.  First, position is specified as vectors on a
three-dimensional cartesian coordinate system, X, Y, and Z specifying the dimensions of width, height
and depth accordingly.  Floating point color components for their red, green, blue, and alpha channels
or two-dimensional texture coordinate components can also be provided for primitives being draw with
those specific colors or textures rather than a uniform color.  Some of these components are optional,
where OpenGL fills in missing components with sane defaults, and OpenGL expects all these components
in a single, contiguous floating point array where a component count lets it know how many of each
belong to a single vertex it should draw.  Also, OpenGL expects all vertex information on the
graphics hardware where it manipulates it directly, so this must be marshalled there from the JVM
environment where it is first created.  VertexArray aids in directly allocating a floating point
array on the graphics hardware of the correct size to hold all the vertices a renderer will need
OpenGL to draw.  It also aids in specifying how many components OpenGL should consider for each
vertex from a given offset, and through which shader program's attribute location these vertices
should be piped.  This borrows from OpenGL ES 2.0 for Android by Kevin Brothaler.
*/
public class VertexArray {
    // Single precision floating point numbers require 4 bytes each.
    private static final int BYTES_PER_FLOAT = 4;

    // Hold the contiguous byte array allocated on the graphics hardware for floating point numbers.
    private final FloatBuffer floatBuffer;

    // Create an array of vertices from an array of floating point data.
    public VertexArray( float[] vertexData ) {
        /*
        Allocate a large enough byte buffer on the graphics hardware to hold the floating point
        vertex data as a float buffer in native endian order.  Big endian or little endian order
        does not matter as long as it uniformly specified.  Native endian order lets the JVM
        specify what endian order the hardware expects for the host platform.
        */
        floatBuffer = ByteBuffer
            .allocateDirect( vertexData.length * BYTES_PER_FLOAT )
            .order( ByteOrder.nativeOrder() )
            .asFloatBuffer()
            .put( vertexData );
    }

    /*
    Vertex data provided to OpenGL is used within the context of a linked program to interpret the
    vertices as Points, Lines, or Triangles it should draw to screen.  This is accomplished by
    passing the vertex data through the attribute in the program responsible for drawing it which
    OpenGL specifies by location.  The component count lets OpenGL know how many components it should
    read for each vertex, also letting it know what components it must fill with sane defaults.  The
    stride enables packing vertex data for different things within the same VertexBuffer, like vertex
    coordinates and texture coordinates, such that OpenGL can skip over one or the other as it
    consumes the data.
    */
    public void setVertexAttributePointer(
        int offset, int attributeLocation, int componentCount, int stride
    ) {
        // Start at the offset in the buffer.
        floatBuffer.position( offset );
        // Set and enable the the vertex data for the attribute of the specified shader program.
        glVertexAttribPointer( attributeLocation, componentCount, GL_FLOAT, false, stride, floatBuffer );
        glEnableVertexAttribArray( attributeLocation );
        // Reset position to zero for good housekeeping.
        floatBuffer.position( 0 );
    }
}