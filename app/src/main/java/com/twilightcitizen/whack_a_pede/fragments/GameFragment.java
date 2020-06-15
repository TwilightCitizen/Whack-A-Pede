/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.renderers.GameRenderer;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

/*
Game Fragment displays the main game screen to the user.  It provides menu actions for starting,
pausing, resuming, or stopping games; a scoreboard to show the guest or authenticated player's
avatar, current score, and time remaining; and a GLSurfaceView within a frame designated for
displaying the Lawn where all the game's animations and inputs occur.
*/
public class GameFragment extends Fragment {
    private static final int REQUEST_GOOGLE_SIGN_IN = 100;

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
    private AccountViewModel accountViewModel;

    private ImageView imageProfilePic;
    private TextView textDisplayName;

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

    @Override public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );

        imageProfilePic = view.findViewById( R.id.image_profile_pic );
        textDisplayName = view.findViewById( R.id.text_display_name );
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
        setupAccountViewModel();
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
        accountViewModel.getProfilePicUri().observe( requireActivity(), this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( requireActivity(), this::onDisplayNameChanged );
        accountViewModel.getSignedIn().observe( requireActivity(), this::onSignedInChanged );
    }

    @Override public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        NavController navController = NavHostFragment.findNavController( GameFragment.this );

        switch( item.getItemId() ) {
            case R.id.action_play_game:
                gameViewModel.play(); return true;
            case R.id.action_pause_game:
                gameViewModel.pause(); return true;
            case R.id.action_resume_game:
                gameViewModel.resume(); return true;
            case R.id.action_quit_game:
                gameViewModel.quit(); return true;
            case R.id.action_sign_in:
                startGoogleSignIn(); return true;
            case R.id.action_sign_out:
                accountViewModel.signOut(); return true;
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

    private void setupAccountViewModel() {
        accountViewModel = new ViewModelProvider( requireActivity() ).get( AccountViewModel.class );
    }

    private void onGameStateChanged( GameViewModel.State state ) {
        itemPlay.setVisible( state == GameViewModel.State.newGame );
        itemPause.setVisible( state == GameViewModel.State.running );
        itemResume.setVisible( state == GameViewModel.State.paused );
        itemQuit.setVisible( state == GameViewModel.State.paused );
    }

    private void onProfilePicUriChanged( Uri profilePicUri ) {
        Glide.with( requireActivity() ).load( profilePicUri )
            .placeholder( R.drawable.icon_guest_avatar ).into( imageProfilePic );
    }

    private void onDisplayNameChanged( String displayName ) {
        textDisplayName.setText(
            ( displayName == null ? requireActivity().getString( R.string.guest ) : displayName )
        );
    }

    private void onSignedInChanged( boolean signedIn ) {
        itemSignIn.setVisible( !signedIn );
        itemSignOut.setVisible( signedIn );
    }

    private void startGoogleSignIn() {
        GoogleSignInOptions googleSignInOptions =
            new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_SIGN_IN )
                .requestIdToken( getString( R.string.default_web_client_id ) )
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient( requireActivity(), googleSignInOptions );
        Intent googleSignInIntent = googleSignInClient.getSignInIntent();

        startActivityForResult( googleSignInIntent, REQUEST_GOOGLE_SIGN_IN );
        googleSignInClient.signOut();
    }

    // Obtain Google Sign In task for handling Google Sign In result.
    @Override public void onActivityResult(
        int requestCode, int resultCode, @Nullable Intent data
    ) {
        super.onActivityResult( requestCode, resultCode, data );

        // Guard against non-Google Sign In request.
        if( requestCode == REQUEST_GOOGLE_SIGN_IN ) handleGoogleSignInResult( data );
    }

    // Handle the result of the Google Sign In task result.
    private void handleGoogleSignInResult( @Nullable Intent data ) {
        // Obtain asynchronous task in which Google Sign In activity authenticates user.
        Task< GoogleSignInAccount > googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent( data );

        try {
            // Attempt to obtain the authenticated account from the completed Google Sign In task.
            GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult( ApiException.class );

            // Sign the user in.
            if( googleSignInAccount != null ) accountViewModel.signIn( googleSignInAccount );
        } catch( ApiException e ) {
            e.printStackTrace();
        }
    }
}