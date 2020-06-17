/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.viewModels;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/*
Account ViewModel abstracts the necessary details of a logged in user within a module that can
 survive lifecycle events of the activity to which it belongs.
*/
public class AccountViewModel extends ViewModel {
    // Google Sign-In account the active user, if any.
    private GoogleSignInAccount googleSignInAccount;

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

    // User signed in with Google.  Store necessary profile information in mutable live data.
    public void signIn( GoogleSignInAccount googleSignInAccount ) {
        this.googleSignInAccount = googleSignInAccount;

        profilePicUri.setValue( googleSignInAccount.getPhotoUrl() );
        displayName.setValue( googleSignInAccount.getDisplayName() );

        signedIn.setValue( true );
    }

    // User signed out of Google.  Clear necessary profile information from mutable live data.
    public void signOut() {
        googleSignInAccount = null;

        profilePicUri.setValue( null );
        displayName.setValue( null );
        signedIn.setValue( false );
    }
}
