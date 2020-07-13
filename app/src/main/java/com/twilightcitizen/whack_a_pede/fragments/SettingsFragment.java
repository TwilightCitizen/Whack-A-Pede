/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.utilities.SoundUtil;

public class SettingsFragment extends PreferenceFragmentCompat {
    // Context needed for some actions.
    private GameActivity gameActivity;

    // Check the host context on attachment.
    @Override public void onAttach( @NonNull Context context ) {
        super.onAttach( context );
        checkGameActivityHost( context );
    }

    // Load preferences from XML resources.
    @Override public void onCreatePreferences( Bundle savedInstanceState, String rootKey ) {
        addPreferencesFromResource( R.xml.game_settings );
    }

    // Ensure that the host context is a Game Activity.
    private void checkGameActivityHost( Context context ) {
        if( ! ( context instanceof GameActivity ) )
            throw new ClassCastException( "GameActivity must host SettingsFragment" );

        gameActivity = (GameActivity) context;
    }

    // Apply any changes to the Sound Utility on exit.
    @Override public void onStop() {
        super.onStop();
        SoundUtil.setupVolumes( gameActivity );
        SoundUtil.setupBackgroundMusic( gameActivity );
    }
}
