/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.twilightcitizen.whack_a_pede.models.Segment;
import com.twilightcitizen.whack_a_pede.shaders.ColorShader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

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

    private final float[] modelMatrix = new float[ 16 ];
    private final float[] viewMatrix = new float[ 16 ];
    private final float[] modelViewMatrix = new float[ 16 ];

    private Segment lawn;
    private Segment segment;

    private ColorShader colorShader;

    public GameRenderer( Context context ) { this.context = context; }

    // Called when GLSurfaceView is first created with the renderer.  Parameter gl is ignored.
    @Override public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
        // Set the clear color to yellow #FFD946.
        glClearColor( (float) 0xFF / 0xFF, (float) 0xD9 / 0xFF, (float) 0x46 / 0xFF, 0.0f );

        lawn = new Segment( 1.0f, 32 );
        segment = new Segment( 0.5f, 32 );
        colorShader = new ColorShader( context );
    }

    // Called when GLSurfaceView dimensions change. Parameter gl is ignored.
    @Override public void onSurfaceChanged( GL10 gl, int width, int height ) {
        glViewport( 0, 0, width, height );

        final float aspectRatio = width > height ?
            (float) width / (float) height :
            (float) height / (float) width;

        if( width > height ) {
            // Landscape
            orthoM( viewMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f );
            rotateM( viewMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f );
        } else {
            // Portrait
            orthoM( viewMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f );
        }
    }

    // Repeatedly called to draw frames to the GLSurfaceView.  Parameter gl is ignored.
    @Override public void onDrawFrame( GL10 gl ) {
        // Clear the screen.
        glClear( GL_COLOR_BUFFER_BIT );

        positionModelInScene( 0.0f, 0.0f, 0.0f );
        colorShader.use();
        colorShader.setUniforms( modelViewMatrix, 1.0f, 0.0f, 0.0f );
        lawn.bindData( colorShader );
        lawn.draw();

        positionModelInScene( 0.0f, -0.5f, 0.0f );
        colorShader.setUniforms( modelViewMatrix, 0.0f, 0.0f, 1.0f );
        segment.bindData( colorShader );
        segment.draw();
    }

    private void positionModelInScene( float x, float y, float z ) {
        setIdentityM( modelMatrix, 0 );
        translateM( modelMatrix, 0, x, y, z );
        multiplyMM( modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0 );
    }
}
