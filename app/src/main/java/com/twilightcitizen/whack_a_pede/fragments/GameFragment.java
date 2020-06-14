/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.renderers.GameRenderer;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

/*
Game Fragment displays the main game screen to the user.  It provides menu actions for starting,
pausing, resuming, or stopping games; a scoreboard to show the guest or authenticated player's
avatar, current score, and time remaining; and a GLSurfaceView within a frame designated for
displaying the Lawn where all the game's animations and inputs occur.
*/
public class GameFragment extends Fragment {
    // SurfaceView where OpenGL will draw graphics.
    private GLSurfaceView surfaceView;
    // Flag prevents pausing or resuming non-existent renderer.
    private boolean rendererSet = false;

    private Menu menu;

    private MenuItem itemPlay;
    private MenuItem itemPause;
    private MenuItem itemResume;
    private MenuItem itemQuit;
    private MenuItem itemSignIn;
    private MenuItem itemSignOut;

    private GameViewModel gameViewModel;

    @Override public void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
    }

    /*
    At view creation, find the frame for the Lawn within the layout, create the GLSurfaceView to go
    into it, set it up with a renderer, and then add it to the frame, returning the modified view.
    */
    @Override public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        // Get the layout for the GameFragment and the GameFrame in it.
        View view = inflater.inflate( R.layout.fragment_game, container, false );
        FrameLayout frame = view.findViewById( R.id.frame_game );

        // Create the SurfaceView where OpenGL will draw graphics.
        surfaceView = new GLSurfaceView( requireActivity() );

        // Use OpenGL 2.0, and GameRenderer will do the drawing.
        surfaceView.setEGLContextClientVersion( 2 );
        surfaceView.setRenderer( new GameRenderer( requireActivity() ) );

        // Flag the renderer as set for the SurfaceView.
        rendererSet = true;

        // Add the SurfaceView to the GameFrame.
        frame.addView( surfaceView );

        return view;
    }

    // Prevent rendering to GLSurfaceView when the fragment is stopped.
    @Override public void onStop() {
        super.onStop();

        // Pause the SurfaceView when GameFragment stops.
        if( rendererSet ) surfaceView.onPause();
    }

    // Allow rendering to the GLSurfaceView when the fragment is resumed.
    @Override public void onResume() {
        super.onResume();

        // Resume the SurfaceView when GameFragment starts or resumes.
        if( rendererSet ) surfaceView.onResume();

        setupGameViewModel();
    }

    @SuppressLint( "RestrictedApi" ) @Override public void onCreateOptionsMenu(
        @NonNull Menu menu, @NonNull MenuInflater inflater
    ) {
        this.menu = menu;

        inflater.inflate( R.menu.menu_game, menu );

        itemPlay = menu.findItem( R.id.action_play_game );
        itemPause = menu.findItem( R.id.action_pause_game );
        itemResume = menu.findItem( R.id.action_resume_game );
        itemQuit = menu.findItem( R.id.action_quit_game );
        itemSignIn = menu.findItem( R.id.action_sign_in );
        itemSignOut = menu.findItem( R.id.action_sign_out );

        if( menu instanceof MenuBuilder )
            ( (MenuBuilder) menu ).setOptionalIconsVisible( true );

        gameViewModel.getState().observe( requireActivity(), this::onGameStateChanged );
    }

    @Override public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        NavController navController = NavHostFragment.findNavController( GameFragment.this );
        int itemId = item.getItemId();

        switch( itemId ) {
            case R.id.action_play_game:
                gameViewModel.play(); return true;
            case R.id.action_pause_game:
                gameViewModel.pause(); return true;
            case R.id.action_resume_game:
                gameViewModel.resume(); return true;
            case R.id.action_quit_game:
                gameViewModel.quit(); return true;
            case R.id.action_change_settings:
                navController.navigate( R.id.action_game_to_settings ); return true;
            case R.id.action_view_credits:
                navController.navigate( R.id.action_game_to_credits ); return true;
            case R.id.action_view_leaderboard:
                navController.navigate( R.id.action_game_to_leaderboard ); return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private void setupGameViewModel() {
        gameViewModel = new ViewModelProvider( requireActivity() ).get( GameViewModel.class );
    }

    private void onGameStateChanged( GameViewModel.State state ) {
        itemPlay.setVisible( state == GameViewModel.State.newGame );
        itemPause.setVisible( state == GameViewModel.State.running );
        itemResume.setVisible( state == GameViewModel.State.paused );
        itemQuit.setVisible( state == GameViewModel.State.paused );
    }
}