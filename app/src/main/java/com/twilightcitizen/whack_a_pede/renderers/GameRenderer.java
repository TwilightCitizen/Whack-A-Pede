/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.models.Centipede;
import com.twilightcitizen.whack_a_pede.models.GrassHole;
import com.twilightcitizen.whack_a_pede.models.GrassPatch;
import com.twilightcitizen.whack_a_pede.models.HoleDirt;
import com.twilightcitizen.whack_a_pede.models.Lawn;
import com.twilightcitizen.whack_a_pede.models.Segment;
import com.twilightcitizen.whack_a_pede.shaders.ColorShader;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.Matrix.*;

import static com.twilightcitizen.whack_a_pede.viewModels.GameViewModel.*;

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

    // Game ViewModel maintains game state and the position, direction, and speed of game elements.
    private GameViewModel gameViewModel;

    // Model matrix for manipulating models without respect to the entire scene.
    private final float[] modelMatrix = new float[ 16 ];
    // View matrix for orthographic projection of scene from normalized to device coordinates.
    private final float[] viewMatrix = new float[ 16 ];
    // Matrix for entire scene, orthographically projected with all models placed in it.
    private final float[] modelViewMatrix = new float[ 16 ];
    // Matrix for entire scene inverted for touch events.
    private final float[] invertedViewMatrix = new float[ 16 ];

    // Some game models to place in scene.
    private Lawn lawn;
    private HoleDirt holeDirt;
    private GrassPatch grassPatch;
    private GrassHole grassHole;
    private Segment segment;

    // ColorShader program for drawing game models in scene with solid colors to screen.
    private ColorShader colorShader;

    // Accept and store context on creation, and fact check important dimensions
    public GameRenderer( Context context ) {
        this.context = context;
        gameViewModel = new ViewModelProvider( ( ViewModelStoreOwner ) context ).get( GameViewModel.class );
    }

    // Called when a touch event is sent from the GLSurfaceView.
    public void onTouch( float normalizedX, float normalizedY ) {
        // Normalized point coordinates as a vector.  Z and W are normalized to 0 and 1.
        final float[] normalizedPoint = new float[] { normalizedX, normalizedY, 0.0f, 1.0f };
        // Inverted point maps to coordinate space of the game world before projection and etc.
        final float[] invertedPoint = new float[ 4 ];

        // Invert the normalized point to the game world coordinate space.
        multiplyMV( invertedPoint, 0, invertedViewMatrix, 0, normalizedPoint, 0 );

        // Add the inverted point to the game view model's touch points.
        gameViewModel.addTouchPoint( new Point( invertedPoint[ 0 ], invertedPoint[ 1 ] ) );
    }

    // Called when GLSurfaceView is first created with the renderer.  Parameter gl is ignored.
    @Override public void onSurfaceCreated( GL10 gl, EGLConfig config ) {
        // Set the clear color to yellow #FFD946.
        glClearColor( (float) 0xFF / 0xFF, (float) 0xD9 / 0xFF, (float) 0x46 / 0xFF, 0.0f );
        // Set and enable alpha blending for transparency.
        glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );
        glEnable( GL_BLEND );

        // Instantiate game models and ColorShader program for drawing them.
        lawn = new Lawn( LAWN_NORMAL_HEIGHT, LAWN_NORMAL_WIDTH );
        holeDirt = new HoleDirt( HOLE_NORMAL_RADIUS, 32 );
        grassPatch = new GrassPatch( CELL_NORMAL_HEIGHT );
        grassHole = new GrassHole( CELL_NORMAL_HEIGHT, 8 );
        segment = new Segment( SEGMENT_NORMAL_RADIUS, 32 );
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
            orthoM( viewMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f );
            rotateM( viewMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f );
        } else {
            // Portrait orientation or square device.
            orthoM( viewMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1.0f, 1.0f );
        }

        /*
        TODO: Look at options here.

        Different aspect calculations for orthographic projection within an aspect-constrained
        GLSurfaceView in the layout or scaling.
        */

        scaleM( viewMatrix, 0, 1.25f, 1.25f, 1.0f );

        // Invert the viewMatrix to translate touch events into the space.
        invertM( invertedViewMatrix, 0, viewMatrix, 0 );
    }

    // Repeatedly called to draw frames to the GLSurfaceView.  Parameter gl is ignored.
    @Override public void onDrawFrame( GL10 gl ) {
        // Clear the whole screen with the clear color.
        glClear( GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT );

        // Use the stencil buffer to confine all models in scene to the lawn.
        confineSceneToLawn();

        // Use the ColorShader program to draw models into the scene.
        colorShader.use();

        gameViewModel.loop( TimeUtil.getTimeElapsedMillis() );

        /*
        Position some models in the scene, setting the ColorShader's uniforms to the entire
        orthographically projected and rotated view, binding the model's data it and drawing it.
        */

        positionLawnInScene();
        positionHoleDirtInScene();
        positionSegmentsInScene( false );
        positionGrassHolesInScene();
        //positionTurnsInScene();
        positionGrassPatchesInScene();
        positionSegmentsInScene( true );

        // Anything drawn after this point will not be confined to the lawn.
        glDisable( GL_STENCIL_TEST );
    }

    /*
    Confining the scene to the Lawn prevents OpenGL from drawing anything outside of it, clipping
    or discarding fragments that would have been drawn otherwise.  The stencil buffer is another
    buffer on the graphics hardware where scene models can be drawn, but rather than displaying
    these on screen, these can be used to modify other models drawn to the color buffer which are
    are eventually sent to the screen.  This tells OpenGL that for whatever models are drawn, put
    a 2 (arbitrary) for that pixel in the stencil buffer.  Then, for every model drawn to the color
    buffer afterward, check its pixels against the ones in the stencil buffer.  If they are equal to
    2, then keep them. Otherwise, discard them.  This was adapted from research provided by New
    Castle University at https://research.ncl.ac.uk/game/mastersdegree/graphicsforgames/.
    */
    private void confineSceneToLawn() {
        glEnable( GL_STENCIL_TEST );
        glColorMask( false , false , false , false );
        glStencilFunc( GL_ALWAYS , 2, ~0 );
        glStencilOp( GL_REPLACE , GL_REPLACE , GL_REPLACE );
        positionLawnInScene();
        glColorMask( true, true, true, true );
        glStencilFunc( GL_EQUAL , 2, ~0);
        glStencilOp( GL_KEEP, GL_KEEP, GL_KEEP );
    }

    /*
    The positionXInScene methods that proceed all follow the same formula, sometimes within a loop
    where multiples of the model should be drawn:  The model is positioned in the scene, the uniforms
    are set for the color shader program being used, drawing data for the model is bound to the
    program, and the model is drawn.
    */

    /*
    Use the color shader program to draw grass holes at every position where a hole is located in
    the game view model.
    */
    private void positionGrassHolesInScene() {
        for( Point hole : HOLES ) {
            positionModelInScene( hole.x, hole.y,  0.0f );

            colorShader.setUniforms(
                modelViewMatrix,
                ( float ) 0x00 / 0xFF, ( float ) 0xA3 / 0xFF, ( float ) 0x15 / 0xFF, 0.8f
            );

            grassHole.bindData( colorShader );
            grassHole.draw();
        }
    }

    /*
    Use the color shader program to a segment at every position where a turn is located in
    the game view model.  This is available for troubleshooting turns that otherwise have no
    visual element on screen.
    */
    private void positionTurnsInScene() {
        for( Point turn : TURNS ) {
            positionModelInScene( turn.x, turn.y,  0.0f );

            colorShader.setUniforms( modelViewMatrix, 1.0f, 1.0f, 1.0f, 1.0f );

            segment.bindData( colorShader );
            segment.draw();
        }
    }

    /*
    Use the color shader program to draw patches of grass at every position where a hole is not
    located in the game view model.
    */
    private void positionGrassPatchesInScene() {
        for( Point patch : PATCHES ) {
            positionModelInScene( patch.x, patch.y, 0.0f );

            colorShader.setUniforms(
                modelViewMatrix,
                ( float ) 0x00 / 0xFF, ( float ) 0xA3 / 0xFF, ( float ) 0x15 / 0xFF, 0.8f
            );

            grassPatch.bindData( colorShader );
            grassPatch.draw();
        }
    }

    /*
    Use the color shader program to draw a segment wherever a centipede is located in the game
    view model.  isAbove ensures that above/below-ground centipedes are drawn for the correct
    layer and with the correct corresponding color.
    */
    private void positionSegmentsInScene( boolean isAbove ) {
        for( Centipede centipede : GameViewModel.CENTIPEDES ) while( centipede != null ) {
            // Guard against drawing centipede not for this layer.
            if( centipede.getIsAbove() != isAbove ) {
                centipede = centipede.getTail(); continue;
            }

            positionModelInScene( centipede.getPosition().x, centipede.getPosition().y, 0.0f );

            colorShader.setUniforms(
                modelViewMatrix,
                ( float ) ( isAbove ? 0x00 : 0x00 ) / 0xFF,
                ( float ) ( isAbove ? 0xC4 : 0x62 ) / 0xFF,
                ( float ) ( isAbove ? 0xFF : 0x80 ) / 0xFF,
                1.0f
            );

            segment.bindData( colorShader );
            segment.draw();

            centipede = centipede.getTail();
        }
    }

    /*
    Use the color shader program to draw the hole's dirt at every position where a hole is located
    in the game view model.
    */
    private void positionHoleDirtInScene() {
        for( Point hole : HOLES ) {
            positionModelInScene( hole.x, hole.y,  0.0f );

            colorShader.setUniforms(
                modelViewMatrix,
                ( float ) 0x52 / 0xFF, ( float ) 0x41 / 0xFF, ( float ) 0x00 / 0xFF, 1.0f
            );

            holeDirt.bindData( colorShader );
            holeDirt.draw();
        }
    }

    /*
    Use the color shader program to draw lawn in the scene which serves to confine the region within
    which all holes, turns, and centipedes are drawn
    */
    private void positionLawnInScene() {
        positionModelInScene( 0.0f, 0.0f, 0.0f );

        colorShader.setUniforms(
            modelViewMatrix,
                ( float ) 0x00 / 0xFF, ( float ) 0xA3 / 0xFF, ( float ) 0x15 / 0xFF, 1.0f
        );

        lawn.bindData( colorShader );
        lawn.draw();
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
