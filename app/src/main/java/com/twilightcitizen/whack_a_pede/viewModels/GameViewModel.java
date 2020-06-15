/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.twilightcitizen.whack_a_pede.geometry.Point;

public class GameViewModel extends ViewModel {
    public enum State { newGame, paused, running, stopped }

    /*
    The Whack-A-Pede Lawn assumes a square cellular construction, 7 cells across the X axis by 11
    cells across the Y axis.  This accommodates a grid of 3 holes across the X axis by 5 holes across
    the Y axis with turns set on, around, and  between each.  If the height of the Lawn should fill
    the height of the whole viewport with a normalized height of 2, then the width should fill
    2 x ( 7 / 11 ), or approximately 1.27 where the 27 repeats.
    */
    public static final float lawnCellsXAxis = 7.0f;
    public static final float lawnCellsYAxis = 11.0f;
    public static final float lawnCellsRatio = lawnCellsXAxis / lawnCellsYAxis;
    public static final float lawnNormalHeight = 2.0f;
    public static final float lawnNormalWidth = lawnNormalHeight * lawnCellsRatio;

    /*
    Likewise, the square cells should have a height equal to 2 / 11, and a width of 1.27 / 7, both
    of which should be equivalent, and are within acceptable tolerances for floats.  Other useful
    measures can be born from these, such as radii for circles.
    */
    public static final float cellNormalHeight = lawnNormalHeight / lawnCellsYAxis;
    public static final float cellNormalWidth = lawnNormalWidth / lawnCellsXAxis;
    public static final float cellNormalRadius = cellNormalHeight / 2.0f;

    public static final float holeNormalRadius = cellNormalRadius;
    public static final float segmentNormalRadius = cellNormalRadius * 0.8f;

    /*
    Lines along the X and Y axes where the grid of Holes on the Lawn will be placed.
    */

    private static final float[] holesX = new float[] {
        0.0f - cellNormalWidth * 2.0f,
        0.0f,
        0.0f + cellNormalWidth * 2.0f
    };

    private static final float[] holesY = new float[] {
        0.0f - cellNormalHeight * 4.0f,
        0.0f - cellNormalHeight * 2.0f,
        0.0f,
        0.0f + cellNormalHeight * 2.0f,
        0.0f + cellNormalHeight * 4.0f
    };

    public static final Point[] holes = new Point[ holesX.length * holesY.length ];

    static {
        int hole = 0;

        for( float holeX : holesX ) for( float holeY : holesY )
            holes[ hole++ ] = new Point( holeX, holeY, 0.0f );
    }

    /*
    Lines along the X and Y axes where the grid of Turns on the Lawn will be placed.
    */

    private static final float[] turnsX = new float[] {
        0.0f - cellNormalWidth * 3.0f,
        0.0f - cellNormalWidth * 2.0f,
        0.0f - cellNormalWidth * 1.0f,
        0.0f,
        0.0f + cellNormalWidth * 1.0f,
        0.0f + cellNormalWidth * 2.0f,
        0.0f + cellNormalWidth * 3.0f
    };

    private static final float[] turnsY = new float[] {
        0.0f - cellNormalHeight * 5.0f,
        0.0f - cellNormalHeight * 4.0f,
        0.0f - cellNormalHeight * 3.0f,
        0.0f - cellNormalHeight * 2.0f,
        0.0f - cellNormalHeight * 1.0f,
        0.0f,
        0.0f + cellNormalHeight * 1.0f,
        0.0f + cellNormalHeight * 2.0f,
        0.0f + cellNormalHeight * 3.0f,
        0.0f + cellNormalHeight * 4.0f,
        0.0f + cellNormalHeight * 5.0f
    };

    public static final Point[] turns = new Point[ turnsX.length * turnsY.length ];

    static {
        int turn = 0;

        for( float turnX : turnsX ) for( float turnY : turnsY )
            turns[ turn++ ] = new Point( turnX, turnY, 0.0f );
    }

    /*
    Lines along the X and Y axes where Grass Patches on the Lawn will be placed.
    */

    public static final Point[] patches = new Point[ turns.length - holes.length ];

    static {
        int patch = 0;

        patches: for( Point turn : turns ) {
            for( Point hole : holes ) if( turn.equals( hole ) ) continue patches;

            patches[ patch++ ] = new Point( turn.x, turn.y, 0.0f );
        }
    }

    private MutableLiveData< State > state = new MutableLiveData<>( State.newGame );

    public MutableLiveData< State > getState() { return state; }

    public void reset() { state.setValue( State.newGame ); }
    public void pause() { state.setValue( State.paused ); }
    public void play() { state.setValue( State.running ); }
    public void resume() { state.setValue( State.running ); }
    public void quit() { state.setValue( State.stopped ); }
}
