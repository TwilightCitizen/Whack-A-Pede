/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

import java.util.Locale;

/*
Game Over Fragment displays game over statistics to the player and (eventually) syncs these to the
Google Play Games leader boards and achievements.  It resets the game when the player navigates
back or up.
*/
public class GameOverFragment extends Fragment implements GameActivity.BackFragment {
    // Context needed for some actions.
    private GameActivity gameActivity;
    
    // View models for tracking game and account state.
    private GameViewModel gameViewModel;
    private AccountViewModel accountViewModel;

    // Profile pic, display name, score, and time remaining shown in scoreboard.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textRoundsInTime;
    private TextView textAchievements;

    private String rounds;
    private String inTime;

    // Check the host context on attachment.
    @Override public void onAttach( @NonNull Context context ) {
        super.onAttach( context );
        checkGameActivityHost( context );
    }

    // Ensure that the host context is a Game Activity.
    private void checkGameActivityHost( Context context ) {
        if( ! ( context instanceof GameActivity ) )
            throw new ClassCastException( "GameActivity must host GameOverFragment" );

        gameActivity = (GameActivity) context;
    }

    @Nullable @Override public View onCreateView(
        @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate( R.layout.fragment_game_over, container, false );
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
        textRoundsInTime = view.findViewById( R.id.text_rounds_in_time );
        textAchievements = view.findViewById( R.id.text_achievements );
    }

    // Restore view models and allow rendering to the GLSurfaceView when the fragment is resumed.
    @Override public void onResume() {
        super.onResume();

        // Restore view models.
        gameViewModel = new ViewModelProvider( gameActivity ).get( GameViewModel.class );

        accountViewModel = new ViewModelProvider( gameActivity ).get( AccountViewModel.class );

        // Setup observers that will act on changes to score, rounds, and elapsed time.
        gameViewModel.getScore().observe( gameActivity, this::onScoreChanged );
        gameViewModel.getRounds().observe( gameActivity, this::onRoundChanged );
        gameViewModel.getElapsedTimeMillis().observe( gameActivity, this::onElapsedTimeChanged );

        // Setup observers that will action on changes to profile picture and display name.
        accountViewModel.getProfilePicUri().observe( gameActivity, this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( gameActivity, this::onDisplayNameChanged );

        // Will eventually have achievements.
        onAchievementsChanged( 0 );
    }

    // Remove observers on stop so they do not run without context.
    @Override public void onStop() {
        gameViewModel.getScore().removeObservers( gameActivity );
        gameViewModel.getRounds().removeObservers( gameActivity );
        gameViewModel.getElapsedTimeMillis().removeObservers( gameActivity );
        accountViewModel.getProfilePicUri().removeObservers( gameActivity );
        accountViewModel.getDisplayName().removeObservers( gameActivity );

        super.onStop();
    }

    /*
    Reset the game view model on back press.  This should not happen in onStop because the app can
    be interrupted and would then show stats for a new game.  Also stop observers in here to
    prevent nonexistent context issues.
    */
   public boolean onBackPressed() {
       // Navigation controller for back navigation.
       NavController navController = NavHostFragment.findNavController( GameOverFragment.this );

       // Confirm the player's intentions to navigate back.
       new AlertDialog.Builder( gameActivity, R.style.Whackapede_AlertDialog )
           .setIcon( R.drawable.icon_warning )
           .setTitle( R.string.back_confirmation_title )
           .setMessage( R.string.back_confirmation_body )
           .setNegativeButton(  R.string.back_confirmation_no, null )

           .setPositiveButton(
               R.string.back_confirmation_yes,

               ( DialogInterface dialog, int id ) ->  {
                   gameViewModel.reset();

                   navController.popBackStack();
               }
           )

           .show();

       return true;
   }

    // Observer to replace the profile pic when it changes in the account view model.
    private void onProfilePicUriChanged( Uri profilePicUri ) {
        Glide.with( gameActivity ).load( profilePicUri )
            .placeholder( R.drawable.icon_guest_avatar ).into( imageProfilePic );
    }

    // Observer to replace the display name when it changes in the account view model.
    private void onDisplayNameChanged( String displayName ) {
        textDisplayName.setText(
            displayName == null ? gameActivity.getString( R.string.guest ) : displayName
        );
    }

    // Observer to format the final score  when it changes in the game view model.
    private void onScoreChanged( int score ) {
        // Format the final score.
        textScore.setText(
            String.format( Locale.getDefault(), getString( R.string.final_score ), score )
        );
    }

    // Observer to format the total rounds when it changes in the game view model.
    private void onRoundChanged( int rounds ) {
        // Format the final score.
        this.rounds = String.format( Locale.getDefault(), getString( R.string.rounds ), rounds );

        onRoundsInTimeChanged();
    }

    // Observer to update the elapsed time when it changes in the view model.
    private void onElapsedTimeChanged( long timeElapsed ) {
        // Format time remaining as MM:SS.
        inTime = String.format(
            Locale.getDefault(), getString( R.string.in_time ),
            TimeUtil.millisToMinutesAndSeconds( timeElapsed )
        );

        onRoundsInTimeChanged();
    }

    // Combine rounds and time into a single string for the text view.
    private void onRoundsInTimeChanged() {
        // Format the rounds in time.
        textRoundsInTime.setText(
            String.format( Locale.getDefault(), getString( R.string.rounds_in_time ), rounds, inTime
        ) );
    }

    // Observer to format the total achievements when it changes.
    private void onAchievementsChanged( int achievements ) {
        // Format the final score.
        textAchievements.setText(
            String.format( Locale.getDefault(), getString( R.string.achievements ), achievements )
        );
    }
}
