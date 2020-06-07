/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.renderers.GameRenderer;

/*
Game Fragment displays the main game screen to the user.  It provides menu actions for starting,
pausing, resuming, or stopping games; a scoreboard to show the guest or authenticated player's
avatar, current score, and time remaining; and a GLSurfaceView within a frame designated for
displaying the Lawn where all the game's animations and inputs occur.
*/
public class GameFragment extends Fragment {
    // SurfaceView where OpenGL will draw graphics.
    private GLSurfaceView surfaceView;
    // Flag prevents pausing or resuming non-existent renderer.
    private boolean rendererSet = false;

    /*
    At creation, find the frame for the Lawn within the layout, create the GLSurfaceView to go
    into it, set it up with a renderer, and then add it to the frame, returning the modified view.
    */
    @Override public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        // Get the layout for the GameFragment and the GameFrame in it.
        View view = inflater.inflate( R.layout.fragment_game, container, false );
        FrameLayout frame = view.findViewById( R.id.frame_game );

        // Create the SurfaceView where OpenGL will draw graphics.
        surfaceView = new GLSurfaceView( requireActivity() );

        // Use OpenGL 2.0, and GameRenderer will do the drawing.
        surfaceView.setEGLContextClientVersion( 2 );
        surfaceView.setRenderer( new GameRenderer( requireActivity() ) );

        // Flag the renderer as set for the SurfaceView.
        rendererSet = true;

        // Add the SurfaceView to the GameFrame.
        frame.addView( surfaceView );

        return view;
    }

    // Prevent rendering to GLSurfaceView when the fragment is stopped.
    @Override public void onStop() {
        super.onStop();

        // Pause the SurfaceView when GameFragment stops.
        if( rendererSet ) surfaceView.onPause();
    }

    // Allow rendering to the GLSurfaceView when the fragment is resumed.
    @Override public void onResume() {
        super.onResume();

        // Resume the SurfaceView when GameFragment starts or resumes.
        if( rendererSet ) surfaceView.onResume();
    }
}