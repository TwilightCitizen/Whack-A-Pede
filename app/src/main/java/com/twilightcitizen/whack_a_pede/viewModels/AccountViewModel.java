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

public class AccountViewModel extends ViewModel {
    private GoogleSignInAccount googleSignInAccount;

    private MutableLiveData< Boolean > signedIn = new MutableLiveData<>( false );
    private MutableLiveData< Uri > profilePicUri = new MutableLiveData<>( null );
    private MutableLiveData< String > displayName = new MutableLiveData<>( null );

    public MutableLiveData< Boolean > getSignedIn() { return signedIn; }
    public MutableLiveData< Uri > getProfilePicUri() { return profilePicUri; }
    public MutableLiveData< String > getDisplayName() { return displayName; }

    public void signIn( GoogleSignInAccount googleSignInAccount ) {
        this.googleSignInAccount = googleSignInAccount;

        profilePicUri.setValue( googleSignInAccount.getPhotoUrl() );
        displayName.setValue( googleSignInAccount.getDisplayName() );

        signedIn.setValue( true );
    }

    public void signOut() {
        googleSignInAccount = null;

        profilePicUri.setValue( null );
        displayName.setValue( null );
        signedIn.setValue( false );
    }
}
