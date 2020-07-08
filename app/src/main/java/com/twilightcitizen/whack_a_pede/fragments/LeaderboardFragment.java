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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.recyclerViews.AchievementAdapter;
import com.twilightcitizen.whack_a_pede.recyclerViews.LeaderboardAdapter;
import com.twilightcitizen.whack_a_pede.utilities.PlayGamesUtil;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;

import java.util.Locale;

/*
Leaderboard Fragment displays the leaderboard screen to the user.  It shows the player's leaderboard
entry, including the player's achievements, and then the top 25 players' leaderboard entries,
excluding the current player's if he or she is in the top 25.  Menu options allow the player to
view the leaderboard or achievements in the Google Play Games app.
*/
public class LeaderboardFragment extends Fragment {
    // Context needed for some actions.
    private GameActivity gameActivity;

    // View models for tracking game and account state.
    private AccountViewModel accountViewModel;

    // Profile pic, display name, score, and elapsed time shown in player leaderboard entry.
    private ImageView imageProfilePic;
    private TextView textDisplayName;
    private TextView textScore;
    private TextView textRoundsInTime;
    private TextView textPlacement;
    private TextView textTopPlayerEntries;

    // Retrieving and retrieval error constraint views.
    private ConstraintLayout constraintRetrieving;
    private ConstraintLayout constraintRetrievalError;

    // Recycler view for top player entries and signed in player achievements.
    private RecyclerView recyclerLeaderboard;
    private RecyclerView recyclerAchievements;

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
        setupRecyclerLeaderboard( view );
        setupRecyclerAchievement( view );
        setupRetrievalMessages( view );
        setupRetryButton( view );
    }

    private void setupRecyclerLeaderboard( View view ) {
        recyclerLeaderboard = view.findViewById( R.id.recycler_leaderboard );

        recyclerLeaderboard.setHasFixedSize( true );
        recyclerLeaderboard.setLayoutManager( new LinearLayoutManager( gameActivity ) );

        recyclerLeaderboard.addItemDecoration(
            new DividerItemDecoration( gameActivity, LinearLayoutManager.VERTICAL )
        );

        recyclerLeaderboard.addItemDecoration(
            new LeaderboardAdapter.LeaderboardEntryGap(
                getResources().getDimensionPixelSize( R.dimen.default_margin )
            )
        );
    }

    private void setupRecyclerAchievement( View view ) {
        recyclerAchievements = view.findViewById( R.id.recycler_achievements );

        recyclerAchievements.setHasFixedSize( true );

        recyclerAchievements.setLayoutManager( new LinearLayoutManager(
                gameActivity, LinearLayoutManager.HORIZONTAL, false
        ) );

        recyclerLeaderboard.addItemDecoration(
            new DividerItemDecoration( gameActivity, LinearLayoutManager.HORIZONTAL )
        );

        recyclerLeaderboard.addItemDecoration(
            new AchievementAdapter.AchievementEntryGap(
                getResources().getDimensionPixelSize( R.dimen.default_margin )
            )
        );
    }

    // Keep references to the profile pic, display name, score, and elapsed time views.
    private void setupScoreSummary( View view ) {
        imageProfilePic = view.findViewById( R.id.image_profile_pic );
        textDisplayName = view.findViewById( R.id.text_display_name );
        textScore = view.findViewById( R.id.text_score );
        textRoundsInTime = view.findViewById( R.id.text_rounds_in_time );
        textPlacement = view.findViewById( R.id.text_placement );
        textTopPlayerEntries = view.findViewById( R.id.text_top_player_entries );
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

    // Inflate the leaderboard menu on menu creation.
    @Override public void onCreateOptionsMenu(
        @NonNull Menu menu, @NonNull MenuInflater inflater
    ) {
        inflater.inflate( R.menu.menu_leaderboard, menu );
    }

    // Act on selected menu items.
    @Override public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        switch( item.getItemId() ) {
            case R.id.action_view_leaderboard_in_play_games:
                PlayGamesUtil.showLeaderboardOnPlayGames(
                    gameActivity, accountViewModel.getGoogleSignInAccount()
                ); break;
            case R.id.action_view_achievements_in_play_games:
                PlayGamesUtil.showAchievementsOnPlayGames(
                    gameActivity, accountViewModel.getGoogleSignInAccount()
                ); break;
            default: return super.onOptionsItemSelected( item );
        }

        return true;
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
        int maxLeaderboardEntries = getResources().getInteger( R.integer.max_leaderboard_results );

        PlayGamesUtil.getOtherLeaderboardEntries(
            gameActivity,
            accountViewModel.getGoogleSignInAccount(),
            maxLeaderboardEntries,
            this::onGetOtherLeaderboardEntriesSuccess,
            this::onAnyRetrievalFailure
        );

        textTopPlayerEntries.setText( getResources().getQuantityString(
            R.plurals.top_entries, maxLeaderboardEntries, maxLeaderboardEntries
        ) );
    }

    // Show the other player's leaderboard entries and setup the signed in players achievements.
    private void onGetOtherLeaderboardEntriesSuccess(
        AnnotatedData< LeaderboardsClient.LeaderboardScores > leaderboardScoresAnnotatedData
    ) {
        showOtherLeaderboardEntries( leaderboardScoresAnnotatedData );
        setupPlayerAchievements();
    }

    // Show the other player's leaderboard entries.
    private void showOtherLeaderboardEntries(
        AnnotatedData< LeaderboardsClient.LeaderboardScores > leaderboardScoresAnnotatedData
    ) {
        LeaderboardsClient.LeaderboardScores leaderboardScores = leaderboardScoresAnnotatedData.get();

        if( leaderboardScores == null ) return;

        LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(
            gameActivity, leaderboardScores.getScores(), accountViewModel.getPlayerId()
        );

        recyclerLeaderboard.setAdapter( leaderboardAdapter );
    }

    // Setup the achievements unlocked for the signed in player.
    private void setupPlayerAchievements() {
        PlayGamesUtil.getPlayerAchievements(
            gameActivity,
            accountViewModel.getGoogleSignInAccount(),
            this::onGetPlayerAchievementsSuccess,
            this::onAnyRetrievalFailure
        );
    }

    // Show the player's achievements and hide the retrieving message.
    private void onGetPlayerAchievementsSuccess(
        AnnotatedData< AchievementBuffer > achievementBufferAnnotatedData
    ) {
        AchievementAdapter achievementAdapter = new AchievementAdapter(
            gameActivity, achievementBufferAnnotatedData.get()
        );

        recyclerAchievements.setAdapter( achievementAdapter );
        constraintRetrieving.setVisibility( View.GONE );
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
