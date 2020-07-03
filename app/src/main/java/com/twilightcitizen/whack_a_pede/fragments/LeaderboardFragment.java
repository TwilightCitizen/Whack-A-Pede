/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.utilities.PlayGamesUtil;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;

import java.util.Locale;

public class LeaderboardFragment extends Fragment {
    // Context needed for some actions.
    private GameActivity gameActivity;

    // View models for tracking game and account state.
    private AccountViewModel accountViewModel;

    // Profile pic, display name, score, and elapsed time shown in scoreboard.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textRoundsInTime;
    private TextView textPlacement;

    // Retrieving and retrieval error constraint views.
    private ConstraintLayout constraintRetrieving;
    private ConstraintLayout constraintRetrievalError;

    // Check the host context on attachment.
    @Override public void onAttach( @NonNull Context context ) {
        super.onAttach( context );
        checkGameActivityHost( context );
    }

    // Ensure that the host context is a Game Activity.
    private void checkGameActivityHost( Context context ) {
        if( ! ( context instanceof GameActivity ) )
            throw new ClassCastException( "GameActivity must host LeaderboardFragment" );

        gameActivity = (GameActivity) context;
    }

    @Nullable @Override public View onCreateView(
        @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate( R.layout.fragment_leaderboard, container, false );
    }

    // After view creation, keep references to the score summary.
    @Override public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );
        setupScoreSummary( view );
        setupRetrievalMessages( view );
        setupRetryButton( view );
    }

    // Keep references to the profile pic, display name, score, and elapsed time views.
    private void setupScoreSummary( View view ) {
        imageProfilePic = view.findViewById( R.id.image_profile_pic );
        textDisplayName = view.findViewById( R.id.text_display_name );
        textScore = view.findViewById( R.id.text_score );
        textRoundsInTime = view.findViewById( R.id.text_rounds_in_time );
        textPlacement = view.findViewById( R.id.text_placement );
    }

    // Keep references to constraint views for retrieval status.
    private void setupRetrievalMessages( View view ) {
        constraintRetrieving = view.findViewById( R.id.constraint_retrieving );
        constraintRetrievalError = view.findViewById( R.id.constraint_retrieval_error );
    }

    // Restore view models and observers.
    @Override public void onResume() {
        super.onResume();
        setupViewModels();
        setupObservers();
        setupPlayerLeaderboardEntry();
    }

    // Restore view models.
    private void setupViewModels() {
        accountViewModel = new ViewModelProvider( gameActivity ).get( AccountViewModel.class );
    }

    /*
    Setup observers that will act on changes to score, rounds, and elapsed time, and profile picture
    and display name.
    */
    private void setupObservers() {
        accountViewModel.getProfilePicUri().observe( gameActivity, this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( gameActivity, this::onDisplayNameChanged );
    }

    // Setup the leaderboard entry for the signed in player.
    private void setupPlayerLeaderboardEntry() {
        PlayGamesUtil.getPlayerLeaderboardEntry(
            gameActivity,
            accountViewModel.getGoogleSignInAccount(),
            this::onGetPlayerLeaderboardEntrySuccess,
            this::onAnyRetrievalFailure
        );
    }

    // Show the signed in player's leaderboard entry and setup other player leaderboard entries.
    private void onGetPlayerLeaderboardEntrySuccess(
        AnnotatedData< LeaderboardScore > leaderboardScoreAnnotatedData
    ) {
        showPlayerLeaderboardEntry( leaderboardScoreAnnotatedData );
        setupOtherLeaderboardEntries();
    }

    // Show the signed in player's leaderboard entry.
    private void showPlayerLeaderboardEntry(
        AnnotatedData< LeaderboardScore > leaderboardScoreAnnotatedData
    ) {
        LeaderboardScore leaderboardScore = leaderboardScoreAnnotatedData.get();

        if( leaderboardScore == null ) return;

        textScore.setText( String.format(
            Locale.getDefault(),  getString( R.string.top_score ), leaderboardScore.getRawScore()
        ) );

        String scoreTag = leaderboardScore.getScoreTag();
        String[] scoreTagParts = scoreTag.split( "_" );
        int roundsValue = Integer.parseInt( scoreTagParts[ 0 ] );
        long timeValue = Long.parseLong( scoreTagParts[ 1 ] );

        String rounds = getResources().getQuantityString(
            R.plurals.rounds, roundsValue, roundsValue
        );

        String inTime = String.format(
            Locale.getDefault(), getString( R.string.in_time ),
            TimeUtil.millisToMinutesAndSeconds( timeValue )
        );

        textRoundsInTime.setText( String.format(
            Locale.getDefault(), getString( R.string.rounds_in_time ),  rounds, inTime
        ) );

        textPlacement.setText( leaderboardScore.getDisplayRank() );
    }

    // Show the retrieval error message on retrieval failure.
    private void onAnyRetrievalFailure( Exception e ) {
        constraintRetrievalError.setVisibility( View.VISIBLE );
        e.printStackTrace();
    }

    // Setup the leaderboard entries for the top players.
    private void setupOtherLeaderboardEntries() {
        PlayGamesUtil.getOtherLeaderboardEntries(
            gameActivity,
            accountViewModel.getGoogleSignInAccount(),
            this::onGetOtherLeaderboardEntriesSuccess,
            this::onAnyRetrievalFailure
        );
    }

    // Show the other player's leaderboard entries and hide the retrieving message.
    private void onGetOtherLeaderboardEntriesSuccess(
        AnnotatedData< LeaderboardsClient.LeaderboardScores > leaderboardScoresAnnotatedData
    ) {
        showOtherLeaderboardEntries( leaderboardScoresAnnotatedData );
        constraintRetrieving.setVisibility( View.GONE );
    }

    // Show the other player's leaderboard entries.
    private void showOtherLeaderboardEntries(
        AnnotatedData< LeaderboardsClient.LeaderboardScores > leaderboardScoresAnnotatedData
    ) {
        LeaderboardsClient.LeaderboardScores leaderboardScores = leaderboardScoresAnnotatedData.get();

        if( leaderboardScores == null ) return;

        LeaderboardScoreBuffer leaderboardScoreBuffer = leaderboardScores.getScores();

        int count = leaderboardScoreBuffer.getCount();

        LeaderboardScore leaderboardScore = leaderboardScoreBuffer.get( 0 );

        String displayName = leaderboardScore.getScoreHolderDisplayName();
    }

    // Keep reference to the retry button and set it up for taps.
    private void setupRetryButton( View view ) {
        // Button to retry syncing after sync failure.
        Button buttonRetry = view.findViewById( R.id.button_retry );

        buttonRetry.setOnClickListener( this::onRetryClick );
    }

    // Setup the retry button for taps.
    private void onRetryClick( View view ) { setupPlayerLeaderboardEntry(); }

    // Remove observers on stop so they do not run without context.
    @Override public void onStop() {
        removeObservers();
        super.onStop();
    }

    // Remove observers .
    private void removeObservers() {
        accountViewModel.getProfilePicUri().removeObservers( gameActivity );
        accountViewModel.getDisplayName().removeObservers( gameActivity );
    }

    // Observer to replace the profile pic when it changes in the account view model.
    private void onProfilePicUriChanged( Uri profilePicUri ) {
        ImageManager imageManager = ImageManager.create( gameActivity );

        imageManager.loadImage( imageProfilePic, profilePicUri, R.drawable.icon_guest_avatar );
    }

    // Observer to replace the display name when it changes in the account view model.
    private void onDisplayNameChanged( String displayName ) {
        textDisplayName.setText(
            displayName == null ? gameActivity.getString( R.string.guest ) : displayName
        );
    }
}
