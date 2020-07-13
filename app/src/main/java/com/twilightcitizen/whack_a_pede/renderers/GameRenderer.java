/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.renderers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.preference.PreferenceManager;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.models.Centipede;
import com.twilightcitizen.whack_a_pede.models.Lawn;
import com.twilightcitizen.whack_a_pede.models.Segment;
import com.twilightcitizen.whack_a_pede.shaders.TextureShader;
import com.twilightcitizen.whack_a_pede.utilities.TextureUtil;
import com.twilightcitizen.whack_a_pede.utilities.ThemeUtil;
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
    private final Context context;

    // Determine if game world should rotate with device or not.
    private final boolean rotateForLandscape;

    // Game ViewModel maintains game state and the position, direction, and speed of game elements.
    private final GameViewModel gameViewModel;

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
    private Segment segment;

    // TextureShader program for drawing game models in scene with textures to screen.
    private TextureShader textureShader;

    // Textures for the lawn top and bottom;
    private int lawnTop;
    private int lawnBottom;

    // Textures for the centipede head and body to be used by the TextureShader program.
    private int centipedeHeadAbove;
    private int centipedeHeadBelow;
    private int centipedeBodyAbove;
    private int centipedeBodyBelow;

    // Accept and store context on creation, and fact check important dimensions
    public GameRenderer( Context context ) {
        this.context = context;
        gameViewModel = new ViewModelProvider( ( ViewModelStoreOwner ) context ).get( GameViewModel.class );
        this.rotateForLandscape = context.getResources().getBoolean( R.bool.rotate_surface_for_landscape );
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
        glEnable( GL_BLEND );
        glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );

        // Instantiate game models and shader programs for drawing them.
        lawn = new Lawn( LAWN_NORMAL_HEIGHT, LAWN_NORMAL_WIDTH );
        segment = new Segment( CENTIPEDE_NORMAL_HEIGHT, CENTIPEDE_NORMAL_WIDTH );
        textureShader = new TextureShader( context );

        // Get textures from configured theme in default shared preferences.
        ThemeUtil.Theme theme = ThemeUtil.getConfiguredTheme( context );

        // Load textures to be used by the TextureShader program.
        centipedeHeadAbove = TextureUtil.LoadTexture( context, theme.getCentipedeHeadAbove() );
        centipedeHeadBelow = TextureUtil.LoadTexture( context, theme.getCentipedeHeadBelow() );
        centipedeBodyAbove = TextureUtil.LoadTexture( context, theme.getCentipedeBodyAbove() );
        centipedeBodyBelow = TextureUtil.LoadTexture( context, theme.getCentipedeBodyBelow() );
        lawnTop = TextureUtil.LoadTexture( context, theme.getLawnTop() );
        lawnBottom = TextureUtil.LoadTexture( context, theme.getLawnBottom() );
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
            width < height ? (float) width / (float) height : (float) height / (float) width;

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
        if( width > height && rotateForLandscape ) {
            // Landscape orientation.
            orthoM( viewMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, -1.0f, 1.0f );
            rotateM( viewMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f );
        } else {
            // Portrait orientation or square device.
            orthoM( viewMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, -1.0f, 1.0f );
        }

        // Invert the viewMatrix to translate touch events into the space.
        invertM( invertedViewMatrix, 0, viewMatrix, 0 );
    }

    // Repeatedly called to draw frames to the GLSurfaceView.  Parameter gl is ignored.
    @Override public void onDrawFrame( GL10 gl ) {
        // Clear the whole screen with the clear color.
        glClear( GL_COLOR_BUFFER_BIT );

        // Loop the game for the time slice elapsed.
        gameViewModel.loop( TimeUtil.getTimeElapsedMillis() );

        /*
        Position some models in the scene, setting the ColorShader's uniforms to the entire
        orthographically projected and rotated view, binding the model's data it and drawing it.
        */
        positionLawnBottomInScene();
        positionSegmentsInScene( false );
        positionLawnTopInScene();
        positionSegmentsInScene( true );
    }

    /*
    The positionXInScene methods that proceed all follow the same formula, sometimes within a loop
    where multiples of the model should be drawn:  The shader program to use is specified, the model
    is positioned in the scene, the uniforms are set for the shader program being used, drawing data
    for the model is bound to the program, and the model is drawn.
    */


    /*
    Use the texture shader program to draw a segment wherever a centipede is located in the game
    view model.  isAbove ensures that above/below-ground centipedes are drawn for the correct
    layer for which it is called and with the correct corresponding texture.
    */
    private void positionSegmentsInScene( boolean isAbove ) {
        textureShader.use();

        for( Centipede centipede : GameViewModel.CENTIPEDES ) while( centipede != null ) {
            // Guard against drawing centipede not for this layer.
            if( centipede.getIsAbove() != isAbove ) {
                centipede = centipede.getTail(); continue;
            }

            positionModelInScene(
                centipede.getPosition().x, centipede.getPosition().y, centipede.getRotation()
            );

            // Get the right texture for the segment type and ground layer.
            int texture = centipede.getIsAbove() ? (
                centipede.getIsHead() ? centipedeHeadAbove : centipedeBodyAbove
            ) : (
                centipede.getIsHead() ? centipedeHeadBelow : centipedeBodyBelow
            );

            textureShader.setUniforms( modelViewMatrix, texture );

            segment.bindData( textureShader );
            segment.draw();

            centipede = centipede.getTail();
        }
    }

    /*
    Use the texture shader program to draw lawn top in the scene which serves to confine the region
    within which all holes, turns, and centipedes are drawn.
    */
    private void positionLawnTopInScene() {
        textureShader.use();
        positionModelInScene( 0.0f, 0.0f, 0.0f );
        textureShader.setUniforms( modelViewMatrix, lawnTop );
        lawn.bindData( textureShader );
        lawn.draw();
    }

    /*
    Use the color shader program to draw lawn in the scene which serves to confine the region within
    which all holes, turns, and centipedes are drawn
    */
    private void positionLawnBottomInScene() {
        textureShader.use();
        positionModelInScene( 0.0f, 0.0f, 0.0f );
        textureShader.setUniforms( modelViewMatrix, lawnBottom );
        lawn.bindData( textureShader );
        lawn.draw();
    }

    // Position a model in the scene at specific X, Y, and Z cartesian space coordinates. Z is 0.0f.
    private void positionModelInScene( float x, float y, float rotation ) {
        // Give the model its own coordinate space where it can be manipulated alone.
        setIdentityM( modelMatrix, 0 );
        // Move the model to the desired position in its own space.
        translateM( modelMatrix, 0, x, y, ( float ) 0.0 );
        // Rotate the model if needed.
        if( rotation != 0.0f ) rotateM( modelMatrix, 0, rotation, 0.0f, 0.0f, 1.0f );
        // Fix it into the scene where any manipulations effect all models as part of the whole.
        multiplyMM( modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0 );
    }
}
