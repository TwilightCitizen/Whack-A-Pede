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
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.activities.GameActivity;
import com.twilightcitizen.whack_a_pede.preferences.ThemePreviewPreference;
import com.twilightcitizen.whack_a_pede.utilities.SoundUtil;
import com.twilightcitizen.whack_a_pede.utilities.ThemeUtil;

/*
Settings Fragment provides user-configurable settings for music, sound effects, and themes.  The
theme preview uses a custom preference.
*/
public class SettingsFragment extends PreferenceFragmentCompat {
    // Context needed for some actions.
    private GameActivity gameActivity;

    // Check the host context on attachment.
    @Override public void onAttach( @NonNull Context context ) {
        super.onAttach( context );
        checkGameActivityHost( context );
    }

    // Load preferences from XML resources and setup the theme preference click listener.
    @Override public void onCreatePreferences( Bundle savedInstanceState, String rootKey ) {
        addPreferencesFromResource( R.xml.game_settings );
        setupThemePreference();
    }

    // Set theme preference change handler.
    private void setupThemePreference() {
        ListPreference themePreference =
            findPreference( gameActivity.getString( R.string.color_theme_key ) );

        if( themePreference != null )
            themePreference.setOnPreferenceChangeListener( this::onThemePreferenceChange );
    }

    // Change the theme preview when the selected theme changes.
    private boolean onThemePreferenceChange( Preference preference, Object newValue ) {
        ThemePreviewPreference themePreviewPreference =
            findPreference( gameActivity.getString( R.string.color_theme_preview_key ) );

        if( themePreviewPreference == null ) return false;

        themePreviewPreference.setThemeToPreview(
            ThemeUtil.getNamedTheme( gameActivity, (String) newValue )
        );

        return true;
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
