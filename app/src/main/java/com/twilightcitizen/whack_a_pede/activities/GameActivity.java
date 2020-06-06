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

import android.view.Menu;
import android.view.MenuItem;

import com.twilightcitizen.whack_a_pede.R;

public class GameActivity extends AppCompatActivity {

    @Override protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
    }

    @Override public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_game, menu );
        return true;
    }

    @Override public boolean onOptionsItemSelected( MenuItem item ) {
        if( item.getItemId() > 0 ) return true;

        return super.onOptionsItemSelected( item );
    }
}