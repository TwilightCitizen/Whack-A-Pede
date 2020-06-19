/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

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
    // View models for tracking game and account state.
    private GameViewModel gameViewModel;

    // Profile pic, display name, score, and time remaining shown in scoreboard.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textRoundsInTime;
    private TextView textAchievements;

    private String rounds;
    private String inTime;

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
        gameViewModel = new ViewModelProvider( requireActivity() ).get( GameViewModel.class );

        AccountViewModel accountViewModel =
            new ViewModelProvider( requireActivity() ).get( AccountViewModel.class );

        // Setup observers that will act on changes to score, rounds, and elapsed time.
        gameViewModel.getScore().observe( requireActivity(), this::onScoreChanged );
        gameViewModel.getRounds().observe( requireActivity(), this::onRoundChanged );
        gameViewModel.getElapsedTimeMillis().observe( requireActivity(), this::onElapsedTimeChanged );

        // Setup observers that will action on changes to profile picture and display name.
        accountViewModel.getProfilePicUri().observe( requireActivity(), this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( requireActivity(), this::onDisplayNameChanged );

        // Will eventually have achievements.
        onAchievementsChanged( 0 );
    }

    /*
    Reset the game view model on back press.  This should not happen in onStop because the app can
    be interrupted and would then show stats for a new game.
    */
   public boolean onBackPressed() { gameViewModel.reset(); return false; }

    // Observer to replace the profile pic when it changes in the account view model.
    private void onProfilePicUriChanged( Uri profilePicUri ) {
        Glide.with( requireActivity() ).load( profilePicUri )
            .placeholder( R.drawable.icon_guest_avatar ).into( imageProfilePic );
    }

    // Observer to replace the display name when it changes in the account view model.
    private void onDisplayNameChanged( String displayName ) {
        textDisplayName.setText(
            displayName == null ? requireActivity().getString( R.string.guest ) : displayName
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
