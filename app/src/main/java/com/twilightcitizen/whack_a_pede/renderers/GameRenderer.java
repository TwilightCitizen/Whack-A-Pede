/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class GameRenderer implements GLSurfaceView.Renderer {
    // Context will be required for some things later.
    private Context context;

    public GameRenderer( Context context ) { this.context = context; }

    @Override public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
        // Set the clear color to yellow #FFD946.
        glClearColor( (float) 0xFF / 0xFF, (float) 0xD9 / 0xFF, (float) 0x46 / 0xFF, 0.0f );
    }

    @Override public void onSurfaceChanged( GL10 gl, int width, int height ) {

    }

    @Override public void onDrawFrame( GL10 gl ) {
        // Clear the screen.
        glClear( GL_COLOR_BUFFER_BIT );
    }
}
