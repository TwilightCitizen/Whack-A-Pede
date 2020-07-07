/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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

import java.util.Locale;

/*
Play Games Utility provides static methods to simplify the retrieval of leaderboard and achievement
information for the logged in player and the top 25 players.  Methods also simplify the launching
of Google Play Games to view leaderboards or achievements for the game there.
*/
public class PlayGamesUtil {
    private static final int REQUEST_UNUSED = 100;

    public static void syncLeaderboards(
        Context context, GoogleSignInAccount googleSignInAccount,
        int score, int rounds, long time,
        OnSuccessListener< ScoreSubmissionData > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount == null ) {
            onFailureListener.onFailure(
                new IllegalArgumentException( "Google Sign In Account was Null" )
            );

            return;
        }

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

    public static void getPlayerLeaderboardEntry(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< AnnotatedData< LeaderboardScore > > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount == null ) {
            onFailureListener.onFailure(
                new IllegalArgumentException( "Google Sign In Account was Null" )
            );

            return;
        }

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

    public static void getOtherLeaderboardEntries(
        Context context, GoogleSignInAccount googleSignInAccount, int maxLeaderboardEntries,
        OnSuccessListener< AnnotatedData< LeaderboardsClient.LeaderboardScores > > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount == null ) {
            onFailureListener.onFailure(
                new IllegalArgumentException( "Google Sign In Account was Null" )
            );

            return;
        }

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

    public static void getPlayerUnlockedAchievementCount(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< Integer > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount == null ) {
            onFailureListener.onFailure(
                new IllegalArgumentException( "Google Sign In Account was Null" )
            );

            return;
        }

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        achievementsClient
            .load( true )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( achievementBufferAnnotatedData -> {
                AchievementBuffer achievementBuffer = achievementBufferAnnotatedData.get();
                int achievementCount = achievementBuffer == null ? 0 : achievementBuffer.getCount();
                int unlockedAchievementCount = 0;

                if( achievementCount == 0 ) {
                    onSuccessListener.onSuccess( unlockedAchievementCount ); return;
                }

                for( int i = 0; i < achievementCount; i++ )
                    if( achievementBuffer.get( i ).getState() == Achievement.STATE_UNLOCKED )
                        unlockedAchievementCount++;

                onSuccessListener.onSuccess( unlockedAchievementCount );
            } );
    }

    public static void incrementGameCountAchievements(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< Boolean > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount == null ) {
            onFailureListener.onFailure(
                new IllegalArgumentException( "Google Sign In Account was Null" )
            );

            return;
        }

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        achievementsClient
            .unlockImmediate( context.getString( R.string.play_a_game ) )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( aVoid -> increment10GamesAchievement(
                context, achievementsClient, onSuccessListener, onFailureListener
            ) );
    }

    private static void increment10GamesAchievement(
        Context context,
        AchievementsClient achievementsClient,
        OnSuccessListener< Boolean > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        achievementsClient
            .incrementImmediate( context.getString( R.string.play_10_games ), 1 )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( aVoid -> increment50GamesAchievement(
                context, achievementsClient, onSuccessListener, onFailureListener
            ) );
    }

    private static void increment50GamesAchievement(
        Context context,
        AchievementsClient achievementsClient,
        OnSuccessListener< Boolean > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        achievementsClient
            .incrementImmediate( context.getString( R.string.play_50_games ), 1 )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( aVoid -> increment100GamesAchievement(
                context, achievementsClient, onSuccessListener, onFailureListener
            ) );
    }

    private static void increment100GamesAchievement(
        Context context,
        AchievementsClient achievementsClient,
        OnSuccessListener< Boolean > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        achievementsClient
            .incrementImmediate( context.getString( R.string.play_100_games ), 1 )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( onSuccessListener );
    }

    public static void unlock1MillionPointsAchievement(
        Context context, GoogleSignInAccount googleSignInAccount,
        OnSuccessListener< Void > onSuccessListener,
        OnFailureListener onFailureListener
    ) {
        if( googleSignInAccount == null ) {
            onFailureListener.onFailure(
                new IllegalArgumentException( "Google Sign In Account was Null" )
            );

            return;
        }

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( context, googleSignInAccount );

        achievementsClient
            .unlockImmediate( context.getString( R.string.score_1m_points ) )
            .addOnFailureListener( onFailureListener )
            .addOnSuccessListener( onSuccessListener );
    }

    public static void showAchievementsOnPlayGames(
        Activity activity, GoogleSignInAccount googleSignInAccount
    ) {
        if( googleSignInAccount == null ) {
            // TODO: Alert failure.

            return;
        }

        AchievementsClient achievementsClient =
            Games.getAchievementsClient( activity, googleSignInAccount );

        achievementsClient
            .getAchievementsIntent()
            .addOnSuccessListener( ( OnSuccessListener< Intent > ) intent ->
                activity.startActivityForResult( intent, REQUEST_UNUSED ) )
            .addOnFailureListener( ( OnFailureListener ) Throwable::printStackTrace );
    }

    public static void showLeaderboardOnPlayGames(
        Activity activity, GoogleSignInAccount googleSignInAccount
    ) {
        if( googleSignInAccount == null ) {
            // TODO: Alert failure.

            return;
        }

        LeaderboardsClient leaderboardsClient =
            Games.getLeaderboardsClient( activity, googleSignInAccount );

        leaderboardsClient
            .getAllLeaderboardsIntent()
            .addOnSuccessListener( intent ->
                activity.startActivityForResult( intent, REQUEST_UNUSED ) )
            .addOnFailureListener( ( OnFailureListener ) Throwable::printStackTrace );
    }
}
