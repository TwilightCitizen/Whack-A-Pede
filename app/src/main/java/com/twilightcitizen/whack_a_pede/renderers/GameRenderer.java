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

/*
GameRenderer implemented rendering for any GLSurfaceView to which is it set as the renderer.
Behind the scenes, when GameRenderer is set as a GLSurfaceView's renderer, a background thread is
established to check for whether or not rendering to the GLSurfaceView is paused or resumed, and
continually calls onDrawFrame.  onSurfaceCreated is called at creation, and onSurfaceChanged is
called anytime the GLSurfaceView's dimensions change, including after creation.
*/
public class GameRenderer implements GLSurfaceView.Renderer {
    // Context will be required for some things later.
    private Context context;

    public GameRenderer( Context context ) { this.context = context; }

    // Called when GLSurfaceView is first created with the renderer.  Parameter gl is ignored.
    @Override public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
        // Set the clear color to yellow #FFD946.
        glClearColor( (float) 0xFF / 0xFF, (float) 0xD9 / 0xFF, (float) 0x46 / 0xFF, 0.0f );
    }

    // Called when GLSurfaceView dimensions change. Parameter gl is ignored.
    @Override public void onSurfaceChanged( GL10 gl, int width, int height ) {
    }

    // Repeatedly called to draw frames to the GLSurfaceView.  Parameter gl is ignored.
    @Override public void onDrawFrame( GL10 gl ) {
        // Clear the screen.
        glClear( GL_COLOR_BUFFER_BIT );
    }
}
