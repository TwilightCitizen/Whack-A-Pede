package com.twilightcitizen.whack_a_pede.shaders;

import android.content.Context;
import android.util.Log;

import com.twilightcitizen.whack_a_pede.utilities.LoggerUtil;
import com.twilightcitizen.whack_a_pede.utilities.TextResourceUtil;

import static android.opengl.GLES20.*;

/*
Shader encapsulates functionality shared by all OpenGL shader programs in the application.  There
is presently only one derivative shader program for drawing solidly colored graphics (green, blue,
etc.), but this can be later extended with other OpenGL shader programs for drawing graphics that
have been colored with prerendered texture graphics (zebra stripes, astroturf, etc.).  This borrows
from OpenGL ES 2.0 for Android by Kevin Brothaler with much refactoring for better succinctness
and slight performance enhancements.
*/
public class ShaderProgram {
    /*
    TODO: Move Into ColorShader

    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_COLOR = "u_Color";

    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    */

    // Tag for filtering any debug message logged.
    private static final String TAG = "ShaderProgram";

    // Shader program built on the hardware with OpenGL from vertex and fragment shader programs.
    protected int program;

    /*
    Build a shader program on the hardware with OpenGL from vertex and fragment shader programs.
    These are provided as raw text resources and must be read from them into Strings OpenGl
    understands.
    */
    protected ShaderProgram(
        Context context, int vertexShaderId, int fragmentShaderId
    ) {
        String vertexSource = TextResourceUtil.readTextResource( context, vertexShaderId );
        String fragmentSource = TextResourceUtil.readTextResource( context, fragmentShaderId );

        buildProgram( vertexSource, fragmentSource );
    }

    // Tell OpenGL to use this program.  There can be many built on the hardware at any given time.
    public void use() { glUseProgram( program ); }

    /*
    An OpenGL shader program is a pipeline between a few different components that make it up.
    The output of one component becomes the input of the next component in the pipeline.  While
    OpenGL takes care of a few of these components, all OpenGL ES 2.0 programs should be provided
    a vertex shader program and a fragment shader program.  These let OpenGL know how vertices
    should be manipulated before being converted into fragments for drawing to the screen.
    */
    private void buildProgram( String vertexSource, String fragmentSource ) {
        // Compile the vertex and fragment shader sources on the hardware.
        int vertexShader = compileShader( GL_VERTEX_SHADER, vertexSource );
        int fragmentShader = compileShader( GL_FRAGMENT_SHADER, fragmentSource );

        // Link them into an OpenGL program on the hardware.
        linkProgram( vertexShader, fragmentShader );

        // Validate the program OpenGL created when debugging.
        if( LoggerUtil.DEBUGGING ) validateProgram();
    }

    /*
    Attempt to create an OpenGL program.  If this works, attach the vertex and fragment shader
    programs created on the hardware and link them into the program.  Delete the program if
    linking fails and log the results of all operations when debugging.
    */
    private void linkProgram( int vertexShader, int fragmentShader ) {
        // Create an OpenGl program on the hardware.
        program = glCreateProgram();

        // Guard against any failure there and log it.
        if( program == 0 && LoggerUtil.DEBUGGING ) {
            Log.w( TAG, "Could not create new program" );

            return;
        }

        // Attach the vertex and fragment shader programs and link them into the program.
        glAttachShader( program, vertexShader );
        glAttachShader( program, fragmentShader );
        glLinkProgram( program );

        // Hold the status of linking them into the program.
        final int[] linkStatus = new int[ 1 ];

        // Read the status of linking them into the program into the holder.
        glGetProgramiv( program, GL_LINK_STATUS, linkStatus, 0 );

        // Log the linking status when debugging.
        if( LoggerUtil.DEBUGGING ) Log.v(
            TAG, "Results of linking program:" + "\n" + glGetProgramInfoLog( program )
        );

        // Delete the program on linking failure and log this when debugging.
        if( linkStatus[ 0 ] == 0 ) {
            glDeleteProgram( program );

            if( LoggerUtil.DEBUGGING ) Log.w( TAG, "Linking of program failed." );
        }
    }

    /*
    It is possible to write valid GLSL programs that OpenGL can link into a program but which are
    not possible to run or correct for the current configuration of it.  As a for instance, writing
    OpenGl ES 2.0 programs when its version is set for OpenGL ES 1.0 operations.  Program validation
    helps catch scenarios like this and provides useful validation status information that can
    be logged for analysis during debugging.
    */
    private void validateProgram() {
        // Validate the OpenGl program.
        glValidateProgram( program );

        // Hold the validation status.
        final int[] validateStatus = new int[ 1 ];

        // Read the validation status into the holder.
        glGetProgramiv( program, GL_VALIDATE_STATUS, validateStatus, 0 );

        // Log the results of the program validation for analysis.
        Log.v(
            TAG, "Results of validating program: " +
                validateStatus[ 0 ] + "\nLog:" + glGetProgramInfoLog( program )
        );
    }

    /*
    Attempt to create a vertex or fragment shader on the hardware with OpenGL.  Return an empty
    shader when this fails and log it when debugging.  Otherwise, load the vertex or shader
    source code into the created shader and compile it.  Check the compilation status.  Delete any
    shader that failed to compile and log the failure for debugging.  Otherwise, return it.
    */
    private int compileShader( int shaderType, String shaderCode ) {
        // Create a shader on the hardware.
        final int shader = glCreateShader( shaderType );

        // Guard against failure, logging it and returning an empty shader.
        if( shader == 0 ) {
            if( LoggerUtil.DEBUGGING ) Log.w( TAG, "Could not create new shader." );

            return 0;
        }

        // Load the shader source code into it and compile it.
        glShaderSource( shader, shaderCode );
        glCompileShader( shader );

        // Hold the shader compilation status.
        final int[] compileStatus = new int[ 1 ];

        // Read the shader compilation status into the holder.
        glGetShaderiv( shader, GL_COMPILE_STATUS, compileStatus, 0 );

        // Log any compilation failure when debugging.
        if( LoggerUtil.DEBUGGING ) Log.v(
            TAG, "Results of compiling source:" + "\n" +
                shaderCode + "\n" + glGetShaderInfoLog( shader )
        );

        // Delete any shader that failed to compile, logging it, and returning an empty shader.
        if( compileStatus[ 0 ] == 0 ) {
            glDeleteShader( shader );

            if( LoggerUtil.DEBUGGING ) Log.w( TAG, "Compilation of shader failed." );

            return 0;
        }

        // Otherwise, return the compiled shader.
        return shader;
    }
}
