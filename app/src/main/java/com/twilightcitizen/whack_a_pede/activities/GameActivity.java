/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.twilightcitizen.whack_a_pede.R;

/*
GameActivity hosts an ActionBar and a FragmentContainerView which acts as the application's
navigation host, loading subordinate fragments to display and manage.  GameFragment is the
default navigation target and the first fragment users will see at launch.
*/
public class GameActivity extends AppCompatActivity {
    // Setup content view and action bar at creation.
    @Override protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        setupActionBar();
    }

    // Setup the toolbar as an action bar.
    private void setupActionBar() {
        Toolbar toolbar = findViewById( R.id.toolbar );

        setSupportActionBar( toolbar );
    }

    @Override protected void onStart() {
        super.onStart();
        setupActionBarNav();
    }

    private void setupActionBarNav() {
        NavController navController = Navigation.findNavController( this, R.id.nav_host_fragment );

        NavigationUI.setupActionBarWithNavController( this, navController );
    }

    @Override public boolean onSupportNavigateUp() {
        onBackPressed();

        return super.onSupportNavigateUp();
    }

    @Override public void onBackPressed() { super.onBackPressed(); }
}