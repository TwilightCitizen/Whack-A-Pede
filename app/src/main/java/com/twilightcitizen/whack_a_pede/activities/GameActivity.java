/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.activities;

import android.media.AudioManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.utilities.SoundUtil;

/*
GameActivity hosts an ActionBar and a FragmentContainerView which acts as the application's
navigation host, loading subordinate fragments to display and manage.  GameFragment is the
default navigation target and the first fragment users will see at launch.
*/
public class GameActivity extends AppCompatActivity {
    // Navigation controller.
    private NavController navController;

    public NavController getNavController() { return navController; }

    // Interface for fragments that need to act on or consume a back press.
    public interface BackFragment {
        // Return true for consuming back presses or false if not.
        @SuppressWarnings( "SameReturnValue" ) boolean onBackPressed();
    }

    // Setup content view and action bar at creation.
    @Override protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        setupActionBar();
        setupAudio();
    }

    // Setup the toolbar as an action bar.
    private void setupActionBar() {
        Toolbar toolbar = findViewById( R.id.toolbar );

        setSupportActionBar( toolbar );
    }

    // Setup the game audio for media playback with the sound utility.
    private void setupAudio() {
        setVolumeControlStream( AudioManager.STREAM_MUSIC );
        SoundUtil.initialize( getApplicationContext() );
    }

    // Setup action bar navigation on start.
    @Override protected void onStart() {
        super.onStart();
        setupNavController();
    }

    // Provide back/up navigation controller access in action bar.
    private void setupNavController() {
        navController = Navigation.findNavController( this, R.id.nav_host_fragment );

        NavigationUI.setupActionBarWithNavController( this, navController );
    }

    // Navigate up mirrors back navigation.
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();

        return super.onSupportNavigateUp();
    }

    @Override public void onBackPressed() {
        // Get the currently displayed fragment.
        Fragment fragment = getSupportFragmentManager()
            // The navigation host is the first fragment.
            .getFragments().get( 0 )
            // The first fragment of its child manager is current fragment.
            .getChildFragmentManager().getFragments().get( 0 );

        // Guard against non-BackFragment.
        if( !( fragment instanceof BackFragment ) ) {
            navController.navigateUp(); return;
        }

        // Cast it to a BackFragment.
        BackFragment backFragment = (BackFragment) fragment;

        // Notify it of a back press and see if it consumes it.
        if( !backFragment.onBackPressed() ) navController.navigateUp();
    }
}