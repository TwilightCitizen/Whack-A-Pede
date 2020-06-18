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
TextureShader is a derivative ShaderProgram that links TextureVertexShader and TextureFragmentShader,
extracting and exposing the locations of the color and matrix uniforms and the position attribute
used within it by OpenGL on the graphics hardware.  Instances of this program can be used within
a renderer to place vertices at their specified position, transformed uniformly by the provided
matrix and colored uniformly throughout.  This borrows from OpenGL ES 2.0 for Android by Kevin
Brothaler with much refactoring for better succinctness and slight performance enhancements.
*/
public class TextureShader extends ShaderProgram {
    // Uniforms and attributes within the GLSL source to convert to hardware locations.
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String A_POSITION = "a_Position";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    // Hardware locations of uniforms and attributes in the shader program.
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    /*
    Create a TextureShader program from the TextureVertexShader and TextureFragmentShader programs,
    resolving the locations of the matrix and color uniforms, and the position attribute.
    */
    public TextureShader( Context context ) {
        super( context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader );

        uMatrixLocation = glGetUniformLocation( program, U_MATRIX );
        uTextureUnitLocation = glGetUniformLocation( program, U_TEXTURE_UNIT );
        aPositionLocation = glGetAttribLocation( program, A_POSITION );
        aTextureCoordinatesLocation = glGetAttribLocation( program, A_TEXTURE_COORDINATES );
    }

    // Set the matrix and texture uniforms with those provided.
    public void setUniforms( float[] matrix, int textureId ) {
        glUniformMatrix4fv( uMatrixLocation, 1, false, matrix, 0 );
        glActiveTexture( GL_TEXTURE0 );
        glBindTexture( GL_TEXTURE_2D, textureId );
        glUniform1i( uTextureUnitLocation, 0 );
    }

    /*
    Get the position attribute location.  Because this will be different for every vertex drawn by
    TextureShader, the values it consumes will be set from with the renderer where it is used.
    */
    public int getPositionAttributeLocation() { return aPositionLocation; }

    /*
    Get the texture coordinates attribute location.  Because this will be different for every vertex
    drawn by TextureShader, the values it consumes will be set from with the renderer where it is used.
    */
    public int getTextureCoordinatesAttributeLocation() { return aTextureCoordinatesLocation; }
}