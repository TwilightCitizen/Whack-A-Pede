/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.twilightcitizen.whack_a_pede.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

/*
Play Games Utility provides static methods to simplify the retrieval of leaderboard and achievement
information for the logged in player and the top 25 players.  Methods also simplify the launching
of Google Play Games to view leaderboards or achievements for the game there.
*/
public class PlayGamesUtil {
    // Unused request code needed for Play Games intents.
    private static final int REQUEST_UNUSED = 100;

    // Handle failure through listener on null Google Sign-in account, returning bad account boolean status.
    private static boolean failedOnBadAccount(
        GoogleSignInAccount googleSignInAccount, OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount != null ) return false;

        onFailureListener.onFailure(
            new IllegalArgumentException( "Google Sign In Account was Null" )
        );

        return true;
    }

    // Alert on null Google Sign-in account, returning bad account boolean status.
    private static boolean alertedOnBadAccount(
        Activity activity, GoogleSignInAccount googleSignInAccount
    ) {
        if( googleSignInAccount != null ) return false;

        new AlertDialog.Builder( activity, R.style.Whackapede_AlertDialog )
            .setIcon( R.drawable.icon_warning )
            .setTitle( R.string.bad_account_title )
            .setMessage( R.string.bad_account_body )
            .setPositiveButton( R.string.bad_account_okay, null )
            .show();

        return true;
    }

    // Sync the provide score, rounds, and time to the leaderboard with success or failure handling.
    public static void syncLeaderboards(
        Context context, GoogleSignInAccount googleSignInAccount,
        int score, int rounds, long time,
        OnSuccessListener< ScoreSubmissionData > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        // Pack rounds and time into the score tag.
        String roundsAndTime = String.format(
            Locale.getDefault(), context.getString( R.string.rounds_and_time ), rounds, time
        );

        LeaderboardsClient leaderboardsClient =
            Games.getLeaderboardsClient( context, googleSignInAccount );

        leaderboardsClient
            .submitScoreImmediate(
                context.getString( R.string.leaderboard_score_id ), score, roundsAndTime
            )

            .addOnSuccessListener( onSuccessListener )
            .addOnFailureListener( onFailureListener );
    }

    // Get the signed in player leaderboard entry with success or failure handling.
    public static void getPlayerLeaderboardEntry(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< AnnotatedData< LeaderboardScore > > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        LeaderboardsClient leaderboardsClient =
            Games.getLeaderboardsClient( context, googleSignInAccount );

        leaderboardsClient
            .loadCurrentPlayerLeaderboardScore(
                context.getString( R.string.leaderboard_score_id ),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC
            )

            .addOnSuccessListener( onSuccessListener )
            .addOnFailureListener( onFailureListener );
    }

    // Get the top x players' leaderboard entries with success or failure handling.
    public static void getOtherLeaderboardEntries(
        Context context, GoogleSignInAccount googleSignInAccount, int maxLeaderboardEntries,
        OnSuccessListener< AnnotatedData< LeaderboardsClient.LeaderboardScores > > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        LeaderboardsClient leaderboardsClient =
            Games.getLeaderboardsClient( context, googleSignInAccount );

        leaderboardsClient.loadTopScores(
            context.getString( R.string.leaderboard_score_id ),
            LeaderboardVariant.TIME_SPAN_ALL_TIME,
            LeaderboardVariant.COLLECTION_PUBLIC,
            maxLeaderboardEntries
        )

        .addOnSuccessListener( onSuccessListener )
        .addOnFailureListener( onFailureListener );
    }

    // Get the signed-in player unlocked achievement count with success or failure handling.
    public static void getPlayerUnlockedAchievementCount(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< Integer > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        achievementsClient
            .load( true )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( achievementBufferAnnotatedData -> {
                // Player achievements defaults to all available.
                AchievementBuffer achievementBuffer = achievementBufferAnnotatedData.get();
                int achievementCount = achievementBuffer == null ? 0 : achievementBuffer.getCount();
                int unlockedAchievementCount = 0;

                if( achievementCount == 0 ) {
                    onSuccessListener.onSuccess( unlockedAchievementCount ); return;
                }

                // Return only the unlocked achievement count.
                for( int i = 0; i < achievementCount; i++ )
                    if( achievementBuffer.get( i ).getState() == Achievement.STATE_UNLOCKED )
                        unlockedAchievementCount++;

                onSuccessListener.onSuccess( unlockedAchievementCount );
            } );
    }

    // Get the signed in player's achievements with success or failure handling.
    public static void getPlayerAchievements(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< AnnotatedData< AchievementBuffer > > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        achievementsClient
            .load( true )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( onSuccessListener );
    }

    // Increment the signed in player's game count achievements with success or error handling.
    public static void incrementGameCountAchievements(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< Boolean > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        // All but the single-game achievement must be incremented.
        ArrayList< String > incrementalGameCountAchievements = new ArrayList<>( Arrays.asList(
            context.getResources().getStringArray( R.array.incremental_game_count_achievements )
        ) );

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        // Unlock the single-game achievement and increment the remaining ones.
        achievementsClient
            .unlockImmediate( context.getString( R.string.play_a_game ) )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( aVoid -> incrementGameCountAchievements(
                achievementsClient, incrementalGameCountAchievements, onSuccessListener, onFailureListener
            ) );
    }

    // Recursively increment the signed in player's game count achievements with success or error handling.
    private static void incrementGameCountAchievements(
        AchievementsClient achievementsClient, ArrayList< String > incrementalGameCountAchievements,
        OnSuccessListener< Boolean > onSuccessListener, OnFailureListener onFailureListener

    ) {
        if( incrementalGameCountAchievements.size() == 0 ) {
            onSuccessListener.onSuccess( true ); return;
        }

        String achievementToIncrement = incrementalGameCountAchievements.remove( 0 );

        achievementsClient
            .incrementImmediate( achievementToIncrement, 1 )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( aVoid -> incrementGameCountAchievements(
                achievementsClient,incrementalGameCountAchievements, onSuccessListener, onFailureListener
            ) );
    }

    // Recursively increment the provided set of achievements for the signed in player.
    public static void unlockOtherAchievements(
        Context context, GoogleSignInAccount googleSignInAccount, HashSet< String > achievementIDsToUnlock,
        OnSuccessListener< Void > onSuccessListener, OnFailureListener onFailureListener
    ) {
        if( failedOnBadAccount( googleSignInAccount, onFailureListener ) ) return;

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        unlockOtherAchievements(
            achievementsClient, achievementIDsToUnlock, onSuccessListener, onFailureListener
        );
    }

    // Recursively increment the provided set of achievements for the signed in player.
    private static void unlockOtherAchievements(
        AchievementsClient achievementsClient, HashSet< String > achievementIDsToUnlock,
        OnSuccessListener< Void > onSuccessListener, OnFailureListener onFailureListener

    ) {
        if( achievementIDsToUnlock.size() == 0 ) {
            onSuccessListener.onSuccess( null ); return;
        }

        String achievementIdToUnlock = achievementIDsToUnlock.iterator().next();

        achievementIDsToUnlock.remove( achievementIdToUnlock );

        achievementsClient
            .unlockImmediate( achievementIdToUnlock )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( aVoid -> unlockOtherAchievements(
                achievementsClient, achievementIDsToUnlock, onSuccessListener, onFailureListener
            ) );
    }

    // Show the Google Play Games achievement activity.
    public static void showAchievementsOnPlayGames(
        Activity activity, GoogleSignInAccount googleSignInAccount
    ) {
        if( alertedOnBadAccount( activity, googleSignInAccount ) ) return;

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( activity, googleSignInAccount );

        achievementsClient
            .getAchievementsIntent()
            .addOnSuccessListener( intent -> activity.startActivityForResult( intent, REQUEST_UNUSED ) )
            .addOnFailureListener( Throwable::printStackTrace );
    }

    // Show the Google Play Games leaderboard activity.
    public static void showLeaderboardOnPlayGames(
        Activity activity, GoogleSignInAccount googleSignInAccount
    ) {
        if( alertedOnBadAccount( activity, googleSignInAccount ) ) return;

        LeaderboardsClient leaderboardsClient =
            Games.getLeaderboardsClient( activity, googleSignInAccount );

        leaderboardsClient
            .getAllLeaderboardsIntent()
            .addOnSuccessListener( intent ->
                activity.startActivityForResult( intent, REQUEST_UNUSED ) )
            .addOnFailureListener( Throwable::printStackTrace );
    }
}
