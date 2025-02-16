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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.images.ImageManager;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.renderers.GameRenderer;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

import static com.twilightcitizen.whack_a_pede.viewModels.GameViewModel.State;

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
    private MenuItem itemViewLeaderboard;

    // Flag for options menu creation.
    private boolean optionsMenuIsCreated;

    // View models for tracking game and account state.
    private GameViewModel gameViewModel;
    private AccountViewModel accountViewModel;

    // Profile pic, display name, score, and time remaining shown in scoreboard.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textTimeRemaining;

    // Speedometer views for tablets.
    private ProgressBar progressSpeed;
    private TextView textSpeed;

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
        setupScoreboard( view );
        setupSpeedometer( view );
    }

    // Keep references to the profile pic, display name, score, and time remaining views.
    private void setupScoreboard( View view ) {
        imageProfilePic = view.findViewById( R.id.image_profile_pic );
        textDisplayName = view.findViewById( R.id.text_display_name );
        textScore = view.findViewById( R.id.text_score );
        textTimeRemaining = view.findViewById( R.id.text_time_remaining );
    }

    // Keep reference to the speedometer views for tablets.
    private void setupSpeedometer( View view ) {
        if( gameActivity.getResources().getBoolean( R.bool.is_tablet ) ) {
            progressSpeed = view.findViewById( R.id.progress_speed );
            textSpeed = view.findViewById( R.id.text_speed );
        }
    }

    /*
    Pause the game and prevent rendering to GLSurfaceView when the fragment is stopped.  Also
    remove observers so they do not run without context.
     */
    @Override public void onStop() {
        // Pause the game.
        gameViewModel.pause();

        // Remove observers to avoid them running without context.
        removeObservers();

        // Pause the SurfaceView when GameFragment stops.
        if( rendererSet ) gameSurfaceView.onPause();

        super.onStop();
    }

    // Restore view models and allow rendering to the GLSurfaceView when the fragment is resumed.
    @Override public void onResume() {
        setupViewModels();

        /*
        Setup observers that will act on changes initiated by the menu items when selected.  These
        must be reestablished here after first setup in onCreateOptionsMenu because the observers
        change the visibility of key menu options that do not exist at first start.
        */
        if( optionsMenuIsCreated ) setupMenuObservers();
        setupObservers();

        // Resume the SurfaceView when GameFragment starts or resumes.
        if( rendererSet ) gameSurfaceView.onResume();

        super.onResume();
    }

    // Restore view models.
    private void setupViewModels() {
        gameViewModel = new ViewModelProvider( gameActivity ).get( GameViewModel.class );
        accountViewModel = new ViewModelProvider( gameActivity ).get( AccountViewModel.class );
    }

    /*
    On menu creation, keep references to key menu items, set its icon visibility, and setup
    observers that will act on changes initiated by these menu selections.
    */
    @SuppressLint( "RestrictedApi" ) @Override public void onCreateOptionsMenu(
        @NonNull Menu menu, @NonNull MenuInflater inflater
    ) {
        inflater.inflate( R.menu.menu_game, menu );
        setupMenuItems( menu );

        // Make icons visible in the menu.
        if( menu instanceof MenuBuilder )
            ( ( MenuBuilder ) menu ).setOptionalIconsVisible( true );

        /*
        Setup observers that will act on changes initiated by the menu items when selected.  These
        must be established here first rather than at resume because the observers change the
        visibility of key menu options that do not exist at start, but here.
        */
        setupMenuObservers();

        // Flag the menu as created.
        optionsMenuIsCreated = true;
    }

    // Keep key menu item references.
    private void setupMenuItems( Menu menu ) {
        itemPlay = menu.findItem( R.id.action_play_game );
        itemPause = menu.findItem( R.id.action_pause_game );
        itemResume = menu.findItem( R.id.action_resume_game );
        itemQuit = menu.findItem( R.id.action_quit_game );
        itemSignIn = menu.findItem( R.id.action_sign_in );
        itemSignOut = menu.findItem( R.id.action_sign_out );
        itemViewLeaderboard = menu.findItem( R.id.action_view_leaderboard );
    }

    // Setup observers for the game and account view models.
    private void setupObservers() {
        // Setup observers that will act on changes to score and time remaining in game view model.
        gameViewModel.getScore().observe( gameActivity, this::onScoreChanged );
        gameViewModel.getRemainingTimeMillis().observe( gameActivity, this::onTimeRemainingChanged );

        // Setup centipede speed observer for tablets.
        if( gameActivity.getResources().getBoolean( R.bool.is_tablet ) )
            gameViewModel.getCentipedeSpeed().observe( gameActivity, this::onCentipedeSpeedChanged );
    }

    // Setup observers for the game and account view models.
    private void setupMenuObservers() {
        gameViewModel.getState().observe( gameActivity, this::onGameStateChanged );
        accountViewModel.getProfilePicUri().observe( gameActivity, this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( gameActivity, this::onDisplayNameChanged );
        accountViewModel.getIsSignedIn().observe( gameActivity, this::onSignedInChanged );
    }

    // Remove observers to avoid them running without context.
    private void removeObservers() {
        gameViewModel.getState().removeObservers( gameActivity );
        gameViewModel.getScore().removeObservers( gameActivity );
        gameViewModel.getRemainingTimeMillis().removeObservers( gameActivity );
        accountViewModel.getProfilePicUri().removeObservers( gameActivity );
        accountViewModel.getDisplayName().removeObservers( gameActivity );
        accountViewModel.getIsSignedIn().removeObservers( gameActivity );

        // Remove centipede speed observer for tablets.
        if( gameActivity.getResources().getBoolean( R.bool.is_tablet ) )
            gameViewModel.getCentipedeSpeed().removeObservers( gameActivity );
    }

    // Act on selected menu items.
    @Override public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        switch( item.getItemId() ) {
            // Change game state.
            case R.id.action_play_game: gameViewModel.play(); break;
            case R.id.action_pause_game: gameViewModel.pause(); break;
            case R.id.action_resume_game: gameViewModel.resume(); break;
            case R.id.action_quit_game: confirmQuit(); break;
            // Change logged in state.
            case R.id.action_sign_in: accountViewModel.startGoogleSignIn( this ); break;
            case R.id.action_sign_out: accountViewModel.signOut(); break;
            // Navigate to other screens.
            case R.id.action_change_settings:
                gameActivity.getNavController().navigate( R.id.action_game_to_settings ); break;
            case R.id.action_view_credits:
                gameActivity.getNavController().navigate( R.id.action_game_to_credits ); break;
            case R.id.action_view_instructions:
                gameActivity.getNavController().navigate( R.id.action_game_to_instructions ); break;
            case R.id.action_view_leaderboard:
                gameActivity.getNavController().navigate( R.id.action_game_to_leaderboard ); break;
            default: return super.onOptionsItemSelected( item );
        }

        return true;
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

    // Observer to manage actions based on GameViewModel state.
    private void onGameStateChanged( State state ) {
        toggleMenuItemVisibility( state );

        // Navigate to game over screen if needed.
        if( state == State.gameOver ) try {
            gameActivity.getNavController().navigate( R.id.action_game_to_game_over );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        // Guard against non-tablet form factor.
        if( !gameActivity.getResources().getBoolean( R.bool.is_tablet ) ) return;

        manageSpeedometer( state );
    }

    private void manageSpeedometer( State state ) {
        if( state != State.running ) onCentipedeSpeedChanged( 0.0f );
        else gameViewModel.refreshCentipedeSpeed();
    }

    // Toggle menu item visibility based on GameViewModel state.
    private void toggleMenuItemVisibility( State state ) {
        itemPlay.setVisible( state == State.newGame );
        itemPause.setVisible( state == State.running );
        itemResume.setVisible( state == State.paused );
        itemQuit.setVisible( state == State.paused );
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
        itemViewLeaderboard.setVisible( signedIn );
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

    // Observer to update the speedometer for tablets when it changes in the view model.
    private void onCentipedeSpeedChanged( float centipedeSpeed ) {
        float centipedeRange =
            GameViewModel.CENTIPEDE_MAX_SPEED - GameViewModel.CENTIPEDE_START_SPEED;

        float lawnsPerHourRange = 100.0f - 5.0f;

        float lawnsPerHourSpeed = (
            centipedeSpeed - GameViewModel.CENTIPEDE_START_SPEED
        ) * lawnsPerHourRange / centipedeRange + 5.0f;

        int speedToReport = (int) ( centipedeSpeed == 0.0f ? centipedeSpeed :  lawnsPerHourSpeed );

        // Update the speedometer gauge and readout.
        progressSpeed.setProgress( speedToReport );
        textSpeed.setText( String.format( Locale.getDefault(), "%d", speedToReport ) );
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