/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.shaders;

import android.content.Context;

import com.twilightcitizen.whack_a_pede.R;

import static android.opengl.GLES20.*;

/*
ColorShader is a derivative ShaderProgram that links ColorVertexShader and ColorFragmentShader,
extracting and exposing the locations of the color and matrix uniforms and the position attribute
used within it by OpenGL on the graphics hardware.  Instances of this program can be used within
a renderer to place vertices at their specified position, transformed uniformly by the provided
matrix and colored uniformly throughout.  This borrows from OpenGL ES 2.0 for Android by Kevin
Brothaler with much refactoring for better succinctness and slight performance enhancements.
*/
public class ColorShader extends ShaderProgram {
    // Uniforms and attributes within the GLSL source to convert to hardware locations.
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_COLOR = "u_Color";
    protected static final String A_POSITION = "a_Position";

    // Hardware locations of uniforms and attributes in the shader program.
    private final int uMatrixLocation;
    private final int uColorLocation;
    private final int aPositionLocation;

    /*
    Create a ColorShader program from the ColorVertexShader and ColorFragmentShader programs,
    resolving the locations of the matrix and color uniforms, and the position attribute.
    */
    public ColorShader( Context context ) {
        super( context, R.raw.color_vertex_shader, R.raw.color_fragment_shader );

        uMatrixLocation = glGetUniformLocation( program, U_MATRIX );
        uColorLocation = glGetUniformLocation( program, U_COLOR );
        aPositionLocation = glGetAttribLocation( program, A_POSITION );
    }

    // Set the matrix and color uniforms with those provided.
    public void setUniforms( float[] matrix, float r, float g, float b ) {
        glUniformMatrix4fv( uMatrixLocation, 1, false, matrix, 0 );
        glUniform4f( uColorLocation, r, g, b, 1.0f );
    }

    /*
    Get the position attribute location.  Because this will be different for every vertex drawn by
    ColorShader, the values it consumes will be set from with the renderer where it is used.
    */
    public int getPositionAttributeLocation() { return aPositionLocation; }
}