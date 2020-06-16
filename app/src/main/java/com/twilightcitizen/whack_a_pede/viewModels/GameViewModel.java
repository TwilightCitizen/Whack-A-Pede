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
import com.twilightcitizen.whack_a_pede.geometry.Vector;
import com.twilightcitizen.whack_a_pede.models.Centipede;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameViewModel extends ViewModel {
    public enum State { newGame, paused, running, stopped }

    /*
    The Whack-A-Pede Lawn assumes a square cellular construction, 7 cells across the X axis by 11
    cells across the Y axis.  This accommodates a grid of 3 holes across the X axis by 5 holes across
    the Y axis with turns set on, around, and  between each.  If the height of the Lawn should fill
    the height of the whole viewport with a normalized height of 2, then the width should fill
    2 x ( 7 / 11 ), or approximately 1.27 where the 27 repeats.
    */
    public static final float LAWN_CELLS_X_AXIS = 7.0f;
    public static final float LAWN_CELLS_Y_AXIS = 11.0f;
    public static final float LAWN_CELLS_RATIO = LAWN_CELLS_X_AXIS / LAWN_CELLS_Y_AXIS;
    public static final float LAWN_NORMAL_HEIGHT = 2.0f;
    public static final float LAWN_NORMAL_WIDTH = LAWN_NORMAL_HEIGHT * LAWN_CELLS_RATIO;

    /*
    Likewise, the square cells should have a height equal to 2 / 11, and a width of 1.27 / 7, both
    of which should be equivalent, and are within acceptable tolerances for floats.  Other useful
    measures can be born from these, such as radii for circles.
    */
    public static final float CELL_NORMAL_HEIGHT = LAWN_NORMAL_HEIGHT / LAWN_CELLS_Y_AXIS;
    public static final float CELL_NORMAL_WIDTH = LAWN_NORMAL_WIDTH / LAWN_CELLS_X_AXIS;
    public static final float CELL_NORMAL_RADIUS = CELL_NORMAL_HEIGHT / 2.0f;

    public static final float HOLE_NORMAL_RADIUS = CELL_NORMAL_RADIUS;
    public static final float SEGMENT_NORMAL_RADIUS = CELL_NORMAL_RADIUS * 0.8f;
    public static final float SEGMENT_NORMAL_WIDTH = SEGMENT_NORMAL_RADIUS * 2.0f;
    public static final float SEGMENT_NORMAL_HEIGHT = SEGMENT_NORMAL_RADIUS * 2.0f;

    /*
    Lines along the X and Y axes where the grid of Holes on the Lawn will be placed.
    */

    private static final float[] HOLES_X = new float[] {
        0.0f - CELL_NORMAL_WIDTH * 2.0f,
        0.0f,
        0.0f + CELL_NORMAL_WIDTH * 2.0f
    };

    private static final float[] HOLES_Y = new float[] {
        0.0f - CELL_NORMAL_HEIGHT * 4.0f,
        0.0f - CELL_NORMAL_HEIGHT * 2.0f,
        0.0f,
        0.0f + CELL_NORMAL_HEIGHT * 2.0f,
        0.0f + CELL_NORMAL_HEIGHT * 4.0f
    };

    public static final Point[] HOLES = new Point[ HOLES_X.length * HOLES_Y.length ];

    static {
        int hole = 0;

        for( float holeX : HOLES_X ) for( float holeY : HOLES_Y )
            HOLES[ hole++ ] = new Point( holeX, holeY );
    }

    /*
    Lines along the X and Y axes where the grid of Turns on the Lawn will be placed.
    */

    private static final float[] TURNS_X = new float[] {
        0.0f - CELL_NORMAL_WIDTH * 3.0f,
        0.0f - CELL_NORMAL_WIDTH * 2.0f,
        0.0f - CELL_NORMAL_WIDTH * 1.0f,
        0.0f,
        0.0f + CELL_NORMAL_WIDTH * 1.0f,
        0.0f + CELL_NORMAL_WIDTH * 2.0f,
        0.0f + CELL_NORMAL_WIDTH * 3.0f
    };

    private static final float[] TURNS_Y = new float[] {
        0.0f - CELL_NORMAL_HEIGHT * 5.0f,
        0.0f - CELL_NORMAL_HEIGHT * 4.0f,
        0.0f - CELL_NORMAL_HEIGHT * 3.0f,
        0.0f - CELL_NORMAL_HEIGHT * 2.0f,
        0.0f - CELL_NORMAL_HEIGHT * 1.0f,
        0.0f,
        0.0f + CELL_NORMAL_HEIGHT * 1.0f,
        0.0f + CELL_NORMAL_HEIGHT * 2.0f,
        0.0f + CELL_NORMAL_HEIGHT * 3.0f,
        0.0f + CELL_NORMAL_HEIGHT * 4.0f,
        0.0f + CELL_NORMAL_HEIGHT * 5.0f
    };

    public static final Point[] TURNS = new Point[ TURNS_X.length * TURNS_Y.length ];

    static {
        int turn = 0;

        for( float turnX : TURNS_X ) for( float turnY : TURNS_Y )
            TURNS[ turn++ ] = new Point( turnX, turnY );
    }

    /*
    Lines along the X and Y axes where Grass Patches on the Lawn will be placed.
    */

    public static final Point[] PATCHES = new Point[ TURNS.length - HOLES.length ];

    static {
        int patch = 0;

        patches: for( Point turn : TURNS ) {
            for( Point hole : HOLES ) if( turn.equals( hole ) ) continue patches;

            PATCHES[ patch++ ] = new Point( turn.x, turn.y );
        }
    }

    // Player score and time remaining.
    private static final long ROUND_TIME_MILLIS = 10_000;
    private static final int POINTS_PER_SEGMENT = 100;
    private static final long BONUS_MILLIS_PER_SEGMENT = ROUND_TIME_MILLIS;
    private static final int BONUS_POINTS_PER_SECOND = 10;

    private static final float CENTIPEDE_START_SPEED = CELL_NORMAL_WIDTH / 20.0f;

    private static final float CENTIPEDE_MAX_SPEED = CELL_NORMAL_WIDTH / 10f;

    public static final List< Centipede > CENTIPEDES = new ArrayList<>();

    private float centipedeSpeed = CENTIPEDE_START_SPEED;

    static {
        Centipede centipede = new Centipede( new Point( 0.0f, 1.0f ), Vector.down );

        centipede.addTails( 9, Vector.up );
        CENTIPEDES.add( centipede );
    }

    private MutableLiveData< Integer > score = new MutableLiveData<>( 0 );
    private MutableLiveData< Integer > rounds = new MutableLiveData<>( 0 );
    private MutableLiveData< Long > remainingTimeMillis = new MutableLiveData<>( ROUND_TIME_MILLIS );
    private MutableLiveData< Long > totalTimeMillis = new MutableLiveData<>( 0L );

    private MutableLiveData< State > state = new MutableLiveData<>( State.newGame );

    public MutableLiveData< State > getState() { return state; }

    public void reset() {
        if( state.getValue() == State.stopped )
            state.setValue( State.newGame );
        else
            throw new IllegalStateException( "Game Reset while Not Stopped" );
    }

    public void pause() {
        if( state.getValue() == State.running ) state.setValue( State.paused );
    }

    public void play() {
        if( state.getValue() == State.paused || state.getValue() == State.newGame )
            state.setValue( State.running );
        else
            throw new IllegalStateException( "Game Played while Not Paused or New" );
    }

    public void resume() { play(); }

    public void quit() {
        if( state.getValue() == State.paused )
            state.setValue( State.stopped );
        else
            throw new IllegalStateException( "Game Stopped while Not Paused" );
    }

    public void loop( long elapsedTimeMillis  ) {
        if( state.getValue() != State.running ) return;

        totalTimeMillis.postValue( totalTimeMillis.getValue() + elapsedTimeMillis );
        remainingTimeMillis.postValue( remainingTimeMillis.getValue() - elapsedTimeMillis );

        checkForGameOver();
        checkForNextRound();
        attackCentipedes();
        animateCentipedes( elapsedTimeMillis );
    }

    private void checkForGameOver() {}
    private void checkForNextRound() {}
    private void attackCentipedes() {}

    private void animateCentipedes( long elapsedTimeMillis ) {
        // Normalize the elapsed time as a fraction of 1 seconds.
        double interval = TimeUtil.millisToIntervalOfSeconds( elapsedTimeMillis );

        for( Centipede centipede : CENTIPEDES ) {
            while( centipede != null ) {
                Point nextPosition = new Point(
                    centipede.getPosition().x + centipedeSpeed * centipede.getDirection().x,
                    centipede.getPosition().y + centipedeSpeed * centipede.getDirection().y
                );

                animateThroughHoles( centipede, nextPosition );
                animateThroughTurns( centipede, nextPosition );

                centipede = centipede.getTail();
            }
        }
    }

    private void animateThroughHoles( Centipede centipede, Point nextPosition ) {
        for( Point hole : HOLES ) {
            if( !hole.intersectsPathOf( centipede.getPosition(), nextPosition ) ) continue;

            if( centipede.getHole() == hole ) break;

            centipede.setHole( hole );

            centipede.toggleAbove();

            break;
        }
    }

    private Vector getNewDirectionForTurn( Point turn, Vector currentDirection ) {
        Random random = new Random();
        List< Vector > newDirections = new ArrayList<>();

        if( turn.y != 0.0f + CELL_NORMAL_HEIGHT * 5.0f )
            if( !currentDirection.equals( Vector.down ) )
                newDirections.add( Vector.up );

        if( turn.y != 0.0f - CELL_NORMAL_HEIGHT * 5.0f )
            if( !currentDirection.equals( Vector.up ) )
                newDirections.add( Vector.down );

        if( turn.x != 0.0f - CELL_NORMAL_WIDTH * 3.0f )
            if( !currentDirection.equals( Vector.right ) )
                newDirections.add( Vector.left );

        if( turn.x != 0.0f + CELL_NORMAL_WIDTH * 3.0f )
            if( !currentDirection.equals( Vector.left ) )
                newDirections.add( Vector.right );

        return newDirections.get( random.nextInt( newDirections.size() ) );
    }

    private void animateThroughTurns( Centipede centipede, Point nextPosition ) {
        for( Point turn : TURNS ) {
            Point previousPosition = centipede.getPosition();

            if( !turn.intersectsPathOf( previousPosition, nextPosition ) ) continue;

            if( centipede.getTurn() == turn ) break;

            centipede.setTurn( turn );

            if( centipede.getIsHead() )
                centipede.setDirection( getNewDirectionForTurn( turn, centipede.getDirection() ) );
            else
                centipede.setDirection( centipede.getHead().getDirection() );

            if( turn.equals( nextPosition ) ) break;

            if( centipede.getIsHead() ) {
                float travelAfterTurn;

                if( turn.wasPassedVertically( previousPosition, nextPosition ) ) {
                    travelAfterTurn = Math.abs( nextPosition.y - turn.y );
                } else  {
                    travelAfterTurn = Math.abs( nextPosition.x - turn.x );
                }

                Vector direction = centipede.getDirection();

                if( direction.equals( Vector.up ) ) {
                    nextPosition = new Point( turn.x, nextPosition.y + travelAfterTurn );
                } else if( direction.equals( Vector.down ) ) {
                    nextPosition = new Point( turn.x, nextPosition.y - travelAfterTurn );
                } else if( direction.equals( Vector.left ) ) {
                    nextPosition = new Point( nextPosition.x - travelAfterTurn, turn.y );
                } else {
                    nextPosition = new Point( nextPosition.x + travelAfterTurn, turn.y );
                }

                break;
            }

            Vector direction = centipede.getDirection();
            Point headPosition = centipede.getHead().getPosition();

            if( direction.equals( Vector.up ) ) {
                nextPosition = new Point( turn.x, headPosition.y - SEGMENT_NORMAL_HEIGHT );
            } else if( direction.equals( Vector.down ) ) {
                nextPosition = new Point( turn.x, headPosition.y + SEGMENT_NORMAL_HEIGHT );
            } else if( direction.equals( Vector.left ) ) {
                nextPosition = new Point( headPosition.x + SEGMENT_NORMAL_WIDTH, turn.y );
            } else {
                nextPosition = new Point( headPosition.x - SEGMENT_NORMAL_WIDTH, turn.y );
            }

            break;
        }

        centipede.setPosition( nextPosition );
    }
}
