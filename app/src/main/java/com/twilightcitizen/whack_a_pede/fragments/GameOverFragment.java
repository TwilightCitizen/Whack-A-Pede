/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.viewModels.AccountViewModel;
import com.twilightcitizen.whack_a_pede.viewModels.GameViewModel;

public class GameOverFragment extends Fragment {
    // View models for tracking game and account state.
    private GameViewModel gameViewModel;
    private AccountViewModel accountViewModel;

    @Nullable @Override public View onCreateView(
        @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate( R.layout.fragment_game_over, container, false );
    }

    // Restore view models and allow rendering to the GLSurfaceView when the fragment is resumed.
    @Override public void onResume() {
        super.onResume();

        // Restore view models.
        gameViewModel = new ViewModelProvider( requireActivity() ).get( GameViewModel.class );
        accountViewModel = new ViewModelProvider( requireActivity() ).get( AccountViewModel.class );

        gameViewModel.reset();

        // Setup observers that will act on changes to score and time remaining in game view model.
        //gameViewModel.getScore().observe( requireActivity(), this::onScoreChanged );
        //gameViewModel.getRounds().observe( requireActivity(), this::onRoundChanged );
        //gameViewModel.getElapsedTimeMillis().observe( requireActivity(), this::onElapsedTimeChanged );
    }
}
