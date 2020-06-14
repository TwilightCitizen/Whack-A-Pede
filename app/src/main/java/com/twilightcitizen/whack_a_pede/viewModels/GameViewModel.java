/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GameViewModel extends ViewModel {
    public enum State { newGame, paused, running, stopped }

    private MutableLiveData< State > state = new MutableLiveData<>( State.newGame );

    public MutableLiveData< State > getState() { return state; }

    public void reset() { state.setValue( State.newGame ); }
    public void pause() { state.setValue( State.paused ); }
    public void play() { state.setValue( State.running ); }
    public void resume() { state.setValue( State.running ); }
    public void quit() { state.setValue( State.stopped ); }
}
