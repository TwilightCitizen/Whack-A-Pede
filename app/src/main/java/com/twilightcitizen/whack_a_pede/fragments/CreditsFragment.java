/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;

public class CreditsFragment extends Fragment {
    // Check the host context on attachment.
    @Override public void onAttach( @NonNull Context context ) {
        super.onAttach( context );
        checkGameActivityHost( context );
    }

    // Ensure that the host context is a Game Activity.
    private void checkGameActivityHost( Context context ) {
        if( ! ( context instanceof GameActivity ) )
            throw new ClassCastException( "GameActivity must host CreditsFragment" );
    }

    @Nullable @Override public View onCreateView(
        @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate( R.layout.fragment_credits, container, false );
    }
}
