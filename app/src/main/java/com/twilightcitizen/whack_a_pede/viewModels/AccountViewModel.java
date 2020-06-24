/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.viewModels;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.Task;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.fragments.GameFragment;

/*
Account ViewModel abstracts the necessary details of a logged in user within a module that can
 survive lifecycle events of the activity to which it belongs.
*/
public class AccountViewModel extends ViewModel {
    // Request code for Google Sign In.
    public static final int REQUEST_GOOGLE_SIGN_IN = 100;

    // Google Sign-In account the active user, if any.
    private GoogleSignInAccount googleSignInAccount;

    private String playerId;

    /*
    Mutable live data for profile information allows external observers to update
    as needed whenever these values change.
    */
    private final MutableLiveData< Boolean > signedIn = new MutableLiveData<>( false );
    private final MutableLiveData< Uri > profilePicUri = new MutableLiveData<>( null );
    private final MutableLiveData< String > displayName = new MutableLiveData<>( null );

    /*
    Expose mutable live data for profile information to external observers.
    */
    public MutableLiveData< Boolean > getSignedIn() { return signedIn; }
    public MutableLiveData< Uri > getProfilePicUri() { return profilePicUri; }
    public MutableLiveData< String > getDisplayName() { return displayName; }

    // User signed out of Google.  Clear necessary profile information from mutable live data.
    public void signOut() {
        googleSignInAccount = null;

        profilePicUri.setValue( null );
        displayName.setValue( null );
        signedIn.setValue( false );
    }

    // Initiate the Google Sign-In process.
    public void startGoogleSignIn( GameFragment gameFragment ) {
        // Guard against no context.
        Activity activity = gameFragment.getActivity();

        if( activity == null ) return;

        // Configure the sign in options.
        GoogleSignInOptions googleSignInOptions =
            new GoogleSignInOptions.Builder( GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN )
                .build();

        // Get a sign in client with those options.
        GoogleSignInClient googleSignInClient =
            GoogleSignIn.getClient( activity, googleSignInOptions );

        // Start Google Sign-In with the client's intent.
        gameFragment.startActivityForResult(
            googleSignInClient.getSignInIntent(), REQUEST_GOOGLE_SIGN_IN
        );

        // Prevent automatic reuse of the same account at next sign-in attempt.
        googleSignInClient.signOut();
        googleSignInClient.revokeAccess();
    }

    // Handle the result of the Google Sign In task result.
    public void onGoogleSignInResult( Intent data, GameFragment gameFragment ) {
        // Guard against no context.
        Activity activity = gameFragment.getActivity();

        if( activity == null ) return;

        // Obtain asynchronous task in which Google Sign In activity authenticates user.
        Task< GoogleSignInAccount > googleSignInAccountTask =
            GoogleSignIn.getSignedInAccountFromIntent( data );

        try {
            // Attempt to obtain the authenticated account from the completed Google Sign In task.
            GoogleSignInAccount googleSignInAccount =
                googleSignInAccountTask.getResult( ApiException.class );

            // Sign the user in.
            if( googleSignInAccount != null ) {
                this.googleSignInAccount = googleSignInAccount;

                PlayersClient playersClient =
                    Games.getPlayersClient( activity, googleSignInAccount );

                // Convert the Google Sign In to the player account.
                playersClient.getCurrentPlayer().addOnCompleteListener( this::onGetPlayer );
            }
        } catch( ApiException e ) {
            // Alert the player to sign in failure.
            alertSignInFailure( activity );
        }
    }

    // Alert the player to sign in failure.
    private void alertSignInFailure( Activity activity ) {
        new AlertDialog.Builder( activity, R.style.Whackapede_AlertDialog )
            .setIcon( R.drawable.icon_warning )
            .setTitle( R.string.sign_in_fail_title )
            .setMessage( R.string.sign_in_fail_body )

            .setPositiveButton(
                R.string.sign_in_fail_okay, ( DialogInterface dialog, int id ) ->  {}
            )

            .show();
    }

    // Convert the Google Sign In to the player account.
    private void onGetPlayer( @NonNull Task< Player > task ) {
        if( !task.isSuccessful() ) return;

        if( task.getResult() == null ) {
            signOut(); return;
        }

        this.playerId = task.getResult().getPlayerId();
        profilePicUri.setValue( task.getResult().getHiResImageUri() );
        displayName.setValue( task.getResult().getDisplayName() );

        signedIn.setValue( true );
    }
}
