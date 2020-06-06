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

public class GameFragment extends Fragment {
    // SurfaceView where OpenGL will draw graphics.
    private GLSurfaceView surfaceView;
    // Flag prevents pausing or resuming non-existent renderer.
    private boolean rendererSet = false;

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

    @Override public void onStop() {
        super.onStop();

        // Pause the SurfaceView when GameFragment stops.
        if( rendererSet ) surfaceView.onPause();
    }


    @Override public void onResume() {
        super.onResume();

        // Resume the SurfaceView when GameFragment starts or resumes.
        if( rendererSet ) surfaceView.onResume();
    }
}