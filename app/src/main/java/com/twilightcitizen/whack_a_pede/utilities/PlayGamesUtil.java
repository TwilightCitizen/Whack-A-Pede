/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.ScoreSubmissionData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.twilightcitizen.whack_a_pede.R;

import java.util.Locale;

public class PlayGamesUtil {
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
}
