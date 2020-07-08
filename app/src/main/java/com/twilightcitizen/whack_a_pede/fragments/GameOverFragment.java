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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.utilities.PlayGamesUtil;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

import static com.twilightcitizen.whack_a_pede.viewModels.GameViewModel.Sync;

import java.util.HashSet;
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

    // Profile pic, display name, score, and elapsed time shown in scoreboard.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textRoundsInTime;
    private TextView textAchievements;

    // Rounds, time, and achievement count.
    private String rounds;
    private String inTime;
    private int unlockedAchievementCount;

    // Syncing, Synced, and Error Syncing constraint views.
    private ConstraintLayout constraintSyncing;
    private ConstraintLayout constraintSynced;
    private ConstraintLayout constraintError;

    // Flag for confirmation of back press.
    private boolean confirmedBackPress = false;

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

    // After view creation, keep references to the score summary, sync messaging, and retry button views.
    @Override public void onViewCreated(
        @NonNull View view, @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated( view, savedInstanceState );
        setupScoreSummary( view );
        setupSyncMessages( view );
        setupRetryButton( view );
    }

    // Keep references to the profile pic, display name, score, and elapsed time views.
    private void setupScoreSummary( View view ) {
        imageProfilePic = view.findViewById( R.id.image_profile_pic );
        textDisplayName = view.findViewById( R.id.text_display_name );
        textScore = view.findViewById( R.id.text_score );
        textRoundsInTime = view.findViewById( R.id.text_rounds_in_time );
        textAchievements = view.findViewById( R.id.text_achievements );
    }

    // Keep references to constraint views for syncing status.
    private void setupSyncMessages( View view ) {
        constraintSyncing = view.findViewById( R.id.constraint_syncing );
        constraintSynced = view.findViewById( R.id.constraint_synced );
        constraintError = view.findViewById( R.id.constraint_error );
    }

    // Keep reference to the retry button and set it up for taps.
    private void setupRetryButton( View view ) {
        // Button to retry syncing after sync failure.
        Button buttonRetry = view.findViewById( R.id.button_retry );

        buttonRetry.setOnClickListener( this::onRetryClick );
    }

    // Setup the retry button for taps.
    private void onRetryClick( View view ) {
        gameViewModel.setSyncedToLeaderboard( Sync.notSynced );
    }

    // Restore view models and observers.
    @Override public void onResume() {
        super.onResume();
        setupViewModels();
        setupObservers();
    }

    // Restore view models.
    private void setupViewModels() {
        gameViewModel = new ViewModelProvider( gameActivity ).get( GameViewModel.class );
        accountViewModel = new ViewModelProvider( gameActivity ).get( AccountViewModel.class );
    }

    /*
    Setup observers that will act on changes to score, rounds, and elapsed time, and profile picture
    and display name.
    */
    private void setupObservers() {
        gameViewModel.getScore().observe( gameActivity, this::onScoreChanged );
        gameViewModel.getRounds().observe( gameActivity, this::onRoundChanged );
        gameViewModel.getElapsedTimeMillis().observe( gameActivity, this::onElapsedTimeChanged );
        gameViewModel.getLeaderboardSync().observe( gameActivity, this::onLeaderboardSyncChanged );
        accountViewModel.getProfilePicUri().observe( gameActivity, this::onProfilePicUriChanged );
        accountViewModel.getDisplayName().observe( gameActivity, this::onDisplayNameChanged );
    }

    // Remove observers on stop so they do not run without context.
    @Override public void onStop() {
        removeObservers();
        super.onStop();
    }

    // Remove observers .
    private void removeObservers() {
        gameViewModel.getScore().removeObservers( gameActivity );
        gameViewModel.getRounds().removeObservers( gameActivity );
        gameViewModel.getElapsedTimeMillis().removeObservers( gameActivity );
        gameViewModel.getLeaderboardSync().removeObservers( gameActivity );
        accountViewModel.getProfilePicUri().removeObservers( gameActivity );
        accountViewModel.getDisplayName().removeObservers( gameActivity );
    }

    /*
    Reset the game view model on back press.  This should not happen in onStop because the app can
    be interrupted and would then show stats for a new game.  Also stop observers in here to
    prevent nonexistent context issues.
    */
    public boolean onBackPressed() {
        if( !confirmedBackPress ) { confirmBackPress(); return true; }

        removeObservers();
        gameViewModel.reset();

        return false;
    }

   // Confirm the player's intentions to navigate back.
   private void confirmBackPress() {
       new AlertDialog.Builder( gameActivity, R.style.Whackapede_AlertDialog )
           .setIcon( R.drawable.icon_warning )
           .setTitle( R.string.back_confirmation_title )
           .setMessage( R.string.back_confirmation_body )
           .setNegativeButton(  R.string.back_confirmation_no, null )
           .setPositiveButton( R.string.back_confirmation_yes, this::onConfirmBackPress )
           .show();
   }

   // Confirm back press and navigate up.
   private void onConfirmBackPress( DialogInterface dialogInterface, int id ) {
       confirmedBackPress = true;

       removeObservers();
       gameViewModel.reset();
       gameActivity.getNavController().navigateUp();
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
        this.rounds = getResources().getQuantityString( R.plurals.rounds, rounds, rounds );

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

    // Observer to format the unlocked achievement count when it changes.
    private void onUnlockedAchievementCountChanged( int unlockedAchievementCount ) {
        // Format the final score.
        textAchievements.setText( getResources().getQuantityString(
            R.plurals.achievements, unlockedAchievementCount, unlockedAchievementCount
        ) );
    }

    private void onLeaderboardSyncChanged( Sync sync ) {
        // Toggle the visibility of sync status messages.
        toggleSyncMessageVisibility( sync );

        // Get the Google Sign In account.
        GoogleSignInAccount googleSignInAccount = accountViewModel.getGoogleSignInAccount();

        // Guard against syncing more than once.
        if( sync != Sync.notSynced ) return;

        // Guard against syncing a guest player.
        if( googleSignInAccount == null ) {
            gameViewModel.setSyncedToLeaderboard( Sync.nothingToSync );

            confirmedBackPress = true;

            return;
        }

        // Otherwise start syncing to the leaderboard.
        gameViewModel.setSyncedToLeaderboard( Sync.syncing );

        // Sync the score, rounds, and time to the leaderboard.
        PlayGamesUtil.syncLeaderboards(
            gameActivity, googleSignInAccount,
            GameViewModel.getNullCoalescedValue( gameViewModel.getScore(), 0 ),
            GameViewModel.getNullCoalescedValue( gameViewModel.getRounds(), 1 ),
            GameViewModel.getNullCoalescedValue( gameViewModel.getElapsedTimeMillis(), 0L ),
            this::onLeaderboardSyncSuccess, this::onAnySyncFailure
        );
    }

    // Get the player's achievement count before updating achievements.
    private void onLeaderboardSyncSuccess( ScoreSubmissionData scoreSubmissionData ) {
        PlayGamesUtil.getPlayerUnlockedAchievementCount(
            gameActivity, accountViewModel.getGoogleSignInAccount(),
            this::onGetInitialUnlockedAchievementCountSuccess, this::onAnySyncFailure
        );
    }

    // Increment the number of games played on game count achievements.
    private void onGetInitialUnlockedAchievementCountSuccess( int achievementCount ) {
        this.unlockedAchievementCount = achievementCount;

        PlayGamesUtil.incrementGameCountAchievements(
            gameActivity, accountViewModel.getGoogleSignInAccount(),
            this::onIncrementGameCountAchievementSuccess, this::onAnySyncFailure
        );
    }

    // Build list of other achievements and unlock them.
    private void onIncrementGameCountAchievementSuccess( Boolean unlocked ) {
        HashSet< String > achievementIDsToUnlock = new HashSet<>();
        int scoreValue = GameViewModel.getNullCoalescedValue( gameViewModel.getScore(), 0 );
        int roundsValue = GameViewModel.getNullCoalescedValue( gameViewModel.getRounds(), 0 );

        if( scoreValue >= 1_000_000 ) achievementIDsToUnlock.add( getString( R.string.score_1m_points ) );
        if( scoreValue >= 500_000 ) achievementIDsToUnlock.add( getString( R.string.score_500k_points ) );
        if( scoreValue >= 300_000 ) achievementIDsToUnlock.add( getString( R.string.score_300k_points ) );
        if( scoreValue >= 200_000 ) achievementIDsToUnlock.add( getString( R.string.score_200k_points ) );
        if( scoreValue >= 100_000 ) achievementIDsToUnlock.add( getString( R.string.score_100k_points ) );

        if( roundsValue > 500 ) achievementIDsToUnlock.add( getString( R.string.survive_500_rounds ) );
        if( roundsValue > 100 ) achievementIDsToUnlock.add( getString( R.string.survive_100_rounds ) );
        if( roundsValue > 50 ) achievementIDsToUnlock.add( getString( R.string.survive_50_rounds ) );
        if( roundsValue > 10 ) achievementIDsToUnlock.add( getString( R.string.survive_10_rounds ) );
        if( roundsValue > 1 ) achievementIDsToUnlock.add( getString( R.string.survive_a_round ) );

        if( gameViewModel.getHeadWhack() ) achievementIDsToUnlock.add( getString( R.string.head_whack ) );
        if( gameViewModel.getTailsOnly() ) achievementIDsToUnlock.add( getString( R.string.only_tails ) );
        if( gameViewModel.getDoubleElimination() ) achievementIDsToUnlock.add( getString( R.string.double_elimination ) );
        if( gameViewModel.getTripleElimination() ) achievementIDsToUnlock.add( getString( R.string.triple_elimination ) );
        if( gameViewModel.getQuadrupleElimination() ) achievementIDsToUnlock.add( getString( R.string.quadruple_elimination ) );
        if( gameViewModel.getHalfLife() ) achievementIDsToUnlock.add( getString( R.string.half_life ) );

        PlayGamesUtil.unlockOtherAchievements(
            gameActivity, accountViewModel.getGoogleSignInAccount(), achievementIDsToUnlock,
            this::onUnlockOtherAchievementsSuccess, this::onAnySyncFailure
        );
    }

    // Get the achievement count after any achievement unlocks have been applied.
    private void onUnlockOtherAchievementsSuccess( Void aVoid ) {
        PlayGamesUtil.getPlayerUnlockedAchievementCount(
            gameActivity, accountViewModel.getGoogleSignInAccount(),
            this::onGetUnlockedAchievementCountAfterUnlocksSuccess, this::onAnySyncFailure
        );
    }

    // Flag the sync as complete, message it, and remove back navigation confirmation.
    private void onGetUnlockedAchievementCountAfterUnlocksSuccess( int unlockedAchievementCount ) {
        confirmedBackPress = true;

        onUnlockedAchievementCountChanged( unlockedAchievementCount - this.unlockedAchievementCount );
        gameViewModel.setSyncedToLeaderboard( Sync.synced );
    }

    // Flag the sync as incomplete, message it, and remove back navigation confirmation.
    private void onAnySyncFailure( Exception e ) {
        gameViewModel.setSyncedToLeaderboard( Sync.errorSyncing );
        e.printStackTrace();
    }

    // Toggle the visibility of sync status messages.
    private void toggleSyncMessageVisibility( Sync sync ) {
        constraintSyncing.setVisibility( sync == Sync.syncing ? View.VISIBLE : View.GONE );
        constraintSynced.setVisibility( sync == Sync.synced ? View.VISIBLE : View.GONE );
        constraintError.setVisibility( sync == Sync.errorSyncing ? View.VISIBLE : View.GONE );
    }
}
