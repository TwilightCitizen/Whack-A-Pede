/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.twilightcitizen.whack_a_pede.models.GrassPatch;
import com.twilightcitizen.whack_a_pede.models.Lawn;
import com.twilightcitizen.whack_a_pede.models.Segment;
import com.twilightcitizen.whack_a_pede.shaders.ColorShader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import static org.junit.Assert.*;

/*
GameRenderer implemented rendering for any GLSurfaceView to which is it set as the renderer.
Behind the scenes, when GameRenderer is set as a GLSurfaceView's renderer, a background thread is
established to check for whether or not rendering to the GLSurfaceView is paused or resumed, and
continually calls onDrawFrame.  onSurfaceCreated is called at creation, and onSurfaceChanged is
called anytime the GLSurfaceView's dimensions change, including after creation.
*/
public class GameRenderer implements GLSurfaceView.Renderer {
    // Context will be required by shader programs that read in GLSL resource files.
    private Context context;

    // Model matrix for manipulating models without respect to the entire scene.
    private final float[] modelMatrix = new float[ 16 ];
    // View matrix for orthographic projection of scene from normalized to device coordinates.
    private final float[] viewMatrix = new float[ 16 ];
    // Matrix for entire scene, orthographically projected with all models placed in it.
    private final float[] modelViewMatrix = new float[ 16 ];

    /*
    The Whack-A-Pede Lawn assumes a square cellular construction, 7 cells across the X axis by 11
    cells across the Y axis.  This accommodates a grid of 3 holes across the X axis by 5 holes across
    the Y axis with cells set around and in between each for turns.  If the height of the Lawn should
    fill the height of the whole viewport with a normalized height of 2, then the width should fill
    2 x ( 7 / 11 ), or approximately 1.27 where the 27 repeats.
    */
    private static final float lawnCellsXAxis = 7.0f;
    private static final float lawnCellsYAxis = 11.0f;
    private static final float lawnCellsRatio = lawnCellsXAxis / lawnCellsYAxis;
    private static final float lawnNormalHeight = 2.0f;
    private static final float lawnNormalWidth = lawnNormalHeight * lawnCellsRatio;

    /*
    Likewise, the square cells should have a height equal to 2 / 11, and a width of 1.27 / 7, both
    of which should be equivalent, and are within acceptable tolerances for floats.  Other useful
    measures can be born from these, such as radii for circles.
    */
    private static final float cellNormalHeight = lawnNormalHeight / lawnCellsYAxis;
    private static final float cellNormalWidth = lawnNormalWidth / lawnCellsXAxis;
    private static final float cellNormalRadius = cellNormalHeight / 2.0f;

    // Some game models to place in scene.
    private Lawn lawn;
    private GrassPatch grassPatch;
    private Segment segment;

    // ColorShader program for drawing game models in scene with solid colors to screen.
    private ColorShader colorShader;

    // Accept and store context on creation, and fact check important dimensions
    public GameRenderer( Context context ) {
        this.context = context;

        assertEquals( cellNormalHeight, cellNormalWidth, 0.00001f );
    }

    // Called when GLSurfaceView is first created with the renderer.  Parameter gl is ignored.
    @Override public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
        // Set the clear color to yellow #FFD946.
        glClearColor( (float) 0xFF / 0xFF, (float) 0xD9 / 0xFF, (float) 0x46 / 0xFF, 0.0f );
        // Set and enable alpha blending for transparency.
        glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );
        glEnable( GL_BLEND );

        // Instantiate game models and ColorShader program for drawing them.
        lawn = new Lawn( lawnNormalHeight, lawnNormalWidth );
        grassPatch = new GrassPatch( cellNormalHeight );
        segment = new Segment( cellNormalRadius, 32 );
        colorShader = new ColorShader( context );
    }

    // Called when GLSurfaceView dimensions change. Parameter gl is ignored.
    @Override public void onSurfaceChanged( GL10 gl, int width, int height ) {
        // Set OpenGL to use the entire GLSurfaceView as the viewport.
        glViewport( 0, 0, width, height );

        /*
        Determine the aspect ratio for device coordinates based on its orientation.  In landscape
        orientation, width exceeds  height, while in portrait orientation, height exceeds width.
        For the rare (non-existent?) square device, portrait orientation is assumed.
        */
        final float aspectRatio =
            width > height ? (float) width / (float) height : (float) height / (float) width;

        /*
        Apply an orthographic projection to the scene based on the device's orientation.  OpenGL
        assumes 0 as the origin for the X, Y, and Z axes of 3d cartesian coordinate space, while
        devices do not.  This projection extends normalized coordinates through the widest dimension
        of the device coordinate space and maintains a one-for-one correspondence for the narrowest
        one.  This keeps the aspect ratio of models drawn to the screen the same regardless of the
        devices orientation.  Because models are placed in scene with an assumed portrait
        orientation that is taller than it is wide, the entire scene is also rotated sideways for
        the landscape orientation to better accommodate the screen real estate, but maintain the
        position of models in the scene with respect to each other.
        */
        if( width > height ) {
            // Landscape orientation.
            orthoM( viewMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f );
            rotateM( viewMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f );
        } else {
            // Portrait orientation or square device.
            orthoM( viewMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f );
        }
    }

    // Repeatedly called to draw frames to the GLSurfaceView.  Parameter gl is ignored.
    @Override public void onDrawFrame( GL10 gl ) {
        // Clear the screen with the clear color.
        glClear( GL_COLOR_BUFFER_BIT );

        // Use the ColorShader program to draw models into the scene.
        colorShader.use();

        /*
        Position some models in the scene, setting the ColorShader's uniforms to the entire
        orthographically projected and rotated view, binding the model's data it and drawing it.
        */

        positionModelInScene( 0.0f, 0.0f, 0.0f );
        colorShader.setUniforms( modelViewMatrix, 0.0f, 0.5f, 0.0f, 1.0f );
        lawn.bindData( colorShader );
        lawn.draw();

        positionModelInScene( cellNormalWidth / 2.0f, cellNormalHeight / 2.0f, 0.0f );
        colorShader.setUniforms( modelViewMatrix, 0.0f, 0.0f, 1.0f, 1.0f );
        segment.bindData( colorShader );
        segment.draw();

        positionModelInScene( 0.0f, 0.0f, 0.0f );
        colorShader.setUniforms( modelViewMatrix, 0.0f, 0.5f, 0.0f, 0.8f );
        grassPatch.bindData( colorShader );
        grassPatch.draw();
    }

    // Position a model in the scene at specific X, Y, and Z cartesian space coordinates.
    private void positionModelInScene( float x, float y, float z ) {
        // Give the model its own coordinate space where it can be manipulated alone.
        setIdentityM( modelMatrix, 0 );
        // Move the model to the desired position in its own space.
        translateM( modelMatrix, 0, x, y, z );
        // Fix it into the scene where any manipulations effect all models as part of the whole.
        multiplyMM( modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0 );
    }
}
