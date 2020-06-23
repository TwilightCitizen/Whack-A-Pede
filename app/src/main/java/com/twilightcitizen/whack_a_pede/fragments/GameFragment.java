/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.images.ImageManager;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.renderers.GameRenderer;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

import java.util.Locale;

/*
Game Fragment displays the main game screen to the user.  It provides menu actions for starting,
pausing, resuming, or stopping games; a scoreboard to show the guest or authenticated player's
avatar, current score, and time remaining; and a GLSurfaceView within a frame designated for
displaying the Lawn where all the game's animations and inputs occur.
*/
public class GameFragment extends Fragment {
    // Context needed for some actions.
    private GameActivity gameActivity;
    
    // SurfaceView where OpenGL will draw graphics.
    private GLSurfaceView gameSurfaceView;
    // Renderer that OpenGL will use to draw graphics to the SurfaceView.
    private GameRenderer gameRenderer;
    // Flag prevents pausing or resuming non-existent renderer.
    private boolean rendererSet = false;

    // Menu items whose function and visibility depend on game or account state.
    private MenuItem itemPlay;
    private MenuItem itemPause;
    private MenuItem itemResume;
    private MenuItem itemQuit;
    private MenuItem itemSignIn;
    private MenuItem itemSignOut;

    // View models for tracking game and account state.
    private GameViewModel gameViewModel;
    private AccountViewModel accountViewModel;

    // Profile pic, display name, score, and time remaining shown in scoreboard.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textTimeRemaining;

    // Specify existence of options menu at creation.
    @Override public void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
    }

    // Check the host context on attachment.
    @Override public void onAttach( @NonNull Context context ) {
        super.onAttach( context );
        checkGameActivityHost( context );
    }

    // Ensure that the host context is a Game Activity.
    private void checkGameActivityHost( Context context ) {
        if( ! ( context instanceof GameActivity ) )
            throw new ClassCastException( "GameActivity must host GameFragment" );

        gameActivity = (GameActivity) context;
    }

    /*
        At view creation, find the frame for the Lawn within the layout, create the GLSurfaceView to go
        into it, set it up with a renderer, and then add it to the frame, returning the modified view.
        */
    @SuppressLint( "ClickableViewAccessibility" ) @Override public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        // Get the layout for the GameFragment and the GameFrame in it.
        View view = inflater.inflate( R.layout.fragment_game, container, false );
        FrameLayout frame = view.findViewById( R.id.frame_game );

        // Create the SurfaceView where OpenGL will draw graphics.
        gameSurfaceView = new GLSurfaceView( gameActivity );

        // Create the renderer that OpenGL will use to draw graphics to the SurfaceView.
        gameRenderer = new GameRenderer( gameActivity );

        // Use OpenGL 2.0, and GameRenderer will do the drawing.
        gameSurfaceView.setEGLContextClientVersion( 2 );
        gameSurfaceView.setEGLConfigChooser( true );
        gameSurfaceView.getHolder().setFormat( PixelFormat.RGBA_8888 );
        gameSurfaceView.getHolder().setFormat( PixelFormat.TRANSPARENT );
        gameSurfaceView.setRenderer( gameRenderer );
        gameSurfaceView.setOnTouchListener( this::onTouch );

        // Flag the renderer as set for the SurfaceView.
        rendererSet = true;

        // Add the SurfaceView to the GameFrame.
        frame.addView( gameSurfaceView );

        return view;
    }

    // Forward touches within the SurfaceView to the renderer.
    private boolean onTouch( View view, MotionEvent event ) {
        // Guard against touch events other than a tap.
        if( event == null || event.getAction() != MotionEvent.ACTION_DOWN ) return false;

        // Convert touch coordinates to normalized device coordinates.  Y is inverted.
        final float normalizedX = ( event.getX() / (float) view.getWidth() ) * 2 - 1;
        final float normalizedY = -( ( event.getY() / (float) view.getHeight() ) * 2 - 1 );

        // Forward the normalized touch coordinates to the renderer for handling.
        gameSurfaceView.queueEvent( () -> gameRenderer.onTouch( normalizedX, normalizedY ) );

        return true;
    }

    // After view creation, keep references to the profile pic and display name views.
    @Override public void onViewCreated(
        @NonNull View view, @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated( view, savedInstanceState );

        // Keep references to the profile pic, display name, score, and time remaining views.
        imageProfilePic = view.findViewById( R.id.image_profile_pic );
        textDisplayName = view.findViewById( R.id.text_display_name );
        textScore = view.findViewById( R.id.text_score );
        textTimeRemaining = view.findViewById( R.id.text_time_remaining );
    }

    /*
    Pause the game and prevent rendering to GLSurfaceView when the fragment is stopped.  Also
    remove observers so they do not run without context.
     */
    @Override public void onStop() {
        super.onStop();

        // Pause the game.
        gameViewModel.pause();

        gameViewModel.getState().removeObservers( gameActivity );
        gameViewModel.getScore().removeObservers( gameActivity );
        gameViewModel.getRemainingTimeMillis().removeObservers( gameActivity );
        accountViewModel.getProfilePicUri().removeObservers( gameActivity );
        accountViewModel.getDisplayName().removeObservers( gameActivity );
        accountViewModel.getSignedIn().removeObservers( gameActivity );

        // Pause the SurfaceView when GameFragment stops.
        if( rendererSet ) gameSurfaceView.onPause();
    }

    // Restore view models and allow rendering to the GLSurfaceView when the fragment is resumed.
    @Override public void onResume() {
        super.onResume();

        // Restore view models.
        gameViewModel = new ViewModelProvider( gameActivity ).get( GameViewModel.class );
        accountViewModel = new ViewModelProvider( gameActivity ).get( AccountViewModel.class );

        // Setup observers that will act on changes to score and time remaining in game view model.
        gameViewModel.getScore().observe( gameActivity, this::onScoreChanged );
        gameViewModel.getRemainingTimeMillis().observe( gameActivity, this::onTimeRemainingChanged );

        // Resume the SurfaceView when GameFragment starts or resumes.
        if( rendererSet ) gameSurfaceView.onResume();
    }

    /*
    On menu creation, keep references to key menu items, set its icon visibility, and setup
    observers that will act on changes initiated by these menu selections.
    */
    @SuppressLint( "RestrictedApi" ) @Override public void onCreateOptionsMenu(
        @NonNull Menu menu, @NonNull MenuInflater inflater
    ) {
        inflater.inflate( R.menu.menu_game, menu );

        // Keep key menu item references.
        itemPlay = menu.findItem( R.id.action_play_game );
        itemPause = menu.findItem( R.id.action_pause_game );
        itemResume = menu.findItem( R.id.action_resume_game );
        itemQuit = menu.findItem( R.id.action_quit_game );
        itemSignIn = menu.findItem( R.id.action_sign_in );
        itemSignOut = menu.findItem( R.id.action_sign_out );

        // Make icons visible in the menu.
        if( menu instanceof MenuBuilder )
            ( ( MenuBuilder ) menu ).setOptionalIconsVisible( true );

        /*
        Setup observers that will act on changes initiated by the menu items when selected.  These
        must be established here rather than at resume because the observers change the visibility
        of key menu options that do not exist at start, but here.
        */
        gameViewModel.getState().observe( gameActivity, this::onGameStateChanged );
        accountViewModel.getProfilePicUri().observe( gameActivity, this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( gameActivity, this::onDisplayNameChanged );
        accountViewModel.getSignedIn().observe( gameActivity, this::onSignedInChanged );
    }

    // Act on selected menu items.
    @Override public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        // Navigation controller for navigation items.
        NavController navController = NavHostFragment.findNavController( GameFragment.this );

        switch( item.getItemId() ) {
            // Change game state.
            case R.id.action_play_game:
                gameViewModel.play(); return true;
            case R.id.action_pause_game:
                gameViewModel.pause(); return true;
            case R.id.action_resume_game:
                gameViewModel.resume(); return true;
            case R.id.action_quit_game:
                confirmQuit(); return true;
            // Change logged in state.
            case R.id.action_sign_in:
                accountViewModel.startGoogleSignIn( this ); return true;
            case R.id.action_sign_out:
                accountViewModel.signOut(); return true;
            // Navigate to other screens.
            case R.id.action_change_settings:
                navController.navigate( R.id.action_game_to_settings ); return true;
            case R.id.action_view_credits:
                navController.navigate( R.id.action_game_to_credits ); return true;
            case R.id.action_view_leaderboard:
                navController.navigate( R.id.action_game_to_leaderboard ); return true;
        }

        return super.onOptionsItemSelected( item );
    }

    // Confirm the player's intentions to quit.
    private void confirmQuit() {
        new AlertDialog.Builder( gameActivity, R.style.Whackapede_AlertDialog )
            .setIcon( R.drawable.icon_warning )
            .setTitle( R.string.quit_confirmation_title )
            .setMessage( R.string.quit_confirmation_body )
            .setNegativeButton(  R.string.quit_confirmation_no, null )

            .setPositiveButton(
                R.string.quit_confirmation_yes,

                ( DialogInterface dialog, int id ) ->  gameViewModel.quit()
            )

            .show();
    }

    // Pause the game if the options menu is selected and opened.
    @Override public void onPrepareOptionsMenu( @NonNull Menu menu ) {
        super.onPrepareOptionsMenu( menu );

        // Pause the game if the options menu is selected and opened.
        gameViewModel.pause();
    }

    // Observer to set the visibility of game state menu items depending on its view model state.
    private void onGameStateChanged( GameViewModel.State state ) {
        // Navigation controller for game over navigation.
        NavController navController = NavHostFragment.findNavController( GameFragment.this );

        if( state == GameViewModel.State.gameOver )
            navController.navigate( R.id.action_game_to_game_over );

        itemPlay.setVisible( state == GameViewModel.State.newGame );
        itemPause.setVisible( state == GameViewModel.State.running );
        itemResume.setVisible( state == GameViewModel.State.paused );
        itemQuit.setVisible( state == GameViewModel.State.paused );
    }

    // Observer to replace the profile pic when it changes in the account view model.
    private void onProfilePicUriChanged( Uri profilePicUri ) {
        ImageManager imageManager = ImageManager.create( gameActivity );

        imageManager.loadImage( imageProfilePic, profilePicUri, R.drawable.icon_guest_avatar );
    }

    // Observer to replace the display name when it changes in the account view model.
    private void onDisplayNameChanged( String displayName ) {
        textDisplayName.setText(
            String.format( Locale.getDefault(), "%s:",
            ( displayName == null ? gameActivity.getString( R.string.guest ) : displayName )
        ) );
    }

    // Observer to set the visibility of account menu items when its state changes in the view model.
    private void onSignedInChanged( boolean signedIn ) {
        itemSignIn.setVisible( !signedIn );
        itemSignOut.setVisible( signedIn );
    }

    // Observer to update the score on the scoreboard when it changes in the game view model.
    private void onScoreChanged( int score ) {
        // Format score with commas.
        textScore.setText( String.format( Locale.getDefault(), "%,d", score ) );
    }

    // Observer to update the time remaining on the scoreboard when it changes in the view model.
    private void onTimeRemainingChanged( long timeRemaining ) {
        // Format time remaining as MM:SS.
        textTimeRemaining.setText( TimeUtil.millisToMinutesAndSeconds( timeRemaining ) );
    }

    // Obtain Google Sign In task for handling Google Sign In result.
    @Override public void onActivityResult(
        int requestCode, int resultCode, @Nullable Intent data
    ) {
        super.onActivityResult( requestCode, resultCode, data );

        // Handle Google Sign In request, if any, disregarding other results.
        if( requestCode == AccountViewModel.REQUEST_GOOGLE_SIGN_IN )
            accountViewModel.onGoogleSignInResult( data, this );
    }
}