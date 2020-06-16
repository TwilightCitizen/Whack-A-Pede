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

    // Holes at points generated along the intersections of the previous X and Y axes.
    public static final Point[] HOLES = new Point[ HOLES_X.length * HOLES_Y.length ];

    // Place holes at points generated along the intersections of the previous X and Y axes.
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

    // Turns at points generated along the intersections of the previous X and Y axes.
    public static final Point[] TURNS = new Point[ TURNS_X.length * TURNS_Y.length ];

    // Place turns at points generated along the intersections of the previous X and Y axes.
    static {
        int turn = 0;

        for( float turnX : TURNS_X ) for( float turnY : TURNS_Y )
            TURNS[ turn++ ] = new Point( turnX, turnY );
    }

    // Grass patches at points where turns exist but holes do not.
    public static final Point[] PATCHES = new Point[ TURNS.length - HOLES.length ];

    // Place grass patches at points where turns exist but holes do not.
    static {
        int patch = 0;

        patches: for( Point turn : TURNS ) {
            for( Point hole : HOLES ) if( turn.equals( hole ) ) continue patches;

            PATCHES[ patch++ ] = new Point( turn.x, turn.y );
        }
    }

    // Player scoring and timing constants.
    private static final long ROUND_TIME_MILLIS = 10_000L;
    private static final int POINTS_PER_SEGMENT = 100;
    private static final long BONUS_MILLIS_PER_SEGMENT = ROUND_TIME_MILLIS;
    private static final int BONUS_POINTS_PER_SECOND = 10;

    // Centipede speed constants.
    private static final float CENTIPEDE_START_SPEED = CELL_NORMAL_WIDTH / 20.0f;
    private static final float CENTIPEDE_MAX_SPEED = CELL_NORMAL_WIDTH / 5.0f;

    // This more or less determines the number of rounds before max speed is reached.
    private static final float CENTIPEDE_SPEED_INCREASE =
        ( CENTIPEDE_MAX_SPEED - CENTIPEDE_START_SPEED ) / 1_000.0f;

    // Centipedes on the lawn at any given time.
    public static final List< Centipede > CENTIPEDES = new ArrayList<>();

    // Current speed of centipedes on the lawn.
    private float centipedeSpeed = CENTIPEDE_MAX_SPEED; // CENTIPEDE_START_SPEED;

    /*
    Statically generate a starting centipede for now.
    TODO: Add method to add randomly positioned centipede to lawn.
    */
    static {
        Centipede centipede = new Centipede( new Point( 0.0f, 1.0f ), Vector.down );

        centipede.addTails( 9 );
        CENTIPEDES.add( centipede );
    }

    /*
    Mutable live data for scoring and timing information allows external observers to update
    as needed whenever these values change.
    */
    private MutableLiveData< Integer > score = new MutableLiveData<>( 0 );
    private MutableLiveData< Integer > rounds = new MutableLiveData<>( 0 );
    private MutableLiveData< Long > remainingTimeMillis = new MutableLiveData<>( ROUND_TIME_MILLIS );
    private MutableLiveData< Long > totalTimeMillis = new MutableLiveData<>( 0L );

    /*
    Mutable live data for the game's overall state allows external observers to update as needed
    whenever the game's state changes.
    */
    private MutableLiveData< State > state = new MutableLiveData<>( State.newGame );

    // Expose the mutable live data game state.
    public MutableLiveData< State > getState() { return state; }

    /*
    The following methods allow external game observers to change the game's state as needed.  Calls
    that would move the game between states in an invalid or unexpected way with throw an exception.
    */

    // Only stopped games should be reset to new games.
    public void reset() {
        if( state.getValue() == State.stopped )
            state.setValue( State.newGame );
        else
            throw new IllegalStateException( "Game Reset while Not Stopped" );
    }

    // Only running games should be paused.  Other states basically assumed quasi-paused state.
    public void pause() {
        if( state.getValue() == State.running ) state.setValue( State.paused );
    }

    // Only new or paused games can be started or resumed.
    public void play() {
        if( state.getValue() == State.paused || state.getValue() == State.newGame )
            state.setValue( State.running );
        else
            throw new IllegalStateException( "Game Played while Not Paused or New" );
    }

    // Resume is semantically similar to play, but offers different accessibility feedback.
    public void resume() { play(); }

    // A running game should be paused first.  Otherwise, not yet started to stop or already stopped.
    public void quit() {
        if( state.getValue() == State.paused )
            state.setValue( State.stopped );
        else
            throw new IllegalStateException( "Game Stopped while Not Paused" );
    }

    // Loop the game through the provided time slice.
    public void loop( long elapsedTimeMillis  ) {
        // Guard against changing anything if the game state is not running.
        if( state.getValue() != State.running ) return;

        // Update the time elapsed and time remaining with the provided slice.
        totalTimeMillis.postValue( totalTimeMillis.getValue() + elapsedTimeMillis );
        remainingTimeMillis.postValue( remainingTimeMillis.getValue() - elapsedTimeMillis );

        // Check for game over or next round, then attack and animate centipedes.
        checkForGameOver();
        checkForNextRound();
        attackCentipedes();
        animateCentipedes( elapsedTimeMillis );
    }

    // TODO: Implement These
    private void checkForGameOver() {}
    private void checkForNextRound() {}
    private void attackCentipedes() {}

    // Animate centipedes over the provided time slice.
    private void animateCentipedes( long elapsedTimeMillis ) {
        // Normalize the elapsed time as a fraction of 1 second.
        double interval = TimeUtil.millisToIntervalOfSeconds( elapsedTimeMillis );

        // Animate each centipede, including its tails and their tails.
        for( Centipede centipede : CENTIPEDES ) while( centipede != null ) {
            // Generate a new position for it based on current position, direction, and speed.
            Point nextPosition = new Point(
                centipede.getPosition().x + centipedeSpeed * centipede.getDirection().x,
                centipede.getPosition().y + centipedeSpeed * centipede.getDirection().y
            );

            // Animate through holes and turns separately.
            animateThroughHoles( centipede, nextPosition );
            animateThroughTurns( centipede, nextPosition );

            centipede = centipede.getTail();
        }
    }

    // Animate the centipede through any hole it encountered on its heading.
    private void animateThroughHoles( Centipede centipede, Point nextPosition ) {
        // Check each hole to see if the centipede passed over/under it.
        for( Point hole : HOLES ) {
            // Ignore holes that the centipede did not pass over/under.
            if( !hole.intersectsPathOf( centipede.getPosition(), nextPosition ) ) continue;

            // Guard against time slice issues where the centipede is at the previous frame's hole.
            if( centipede.getHole() == hole ) break;

            // Otherwise, remember the hole and toggle the above/below position.
            centipede.setHole( hole );
            centipede.toggleAbove();

            break;
        }
    }

    // Get a new direction from the encountered turn, excluding going backwards.
    private Vector getNewDirectionForTurn( Point turn, Vector currentDirection ) {
        // Random number generator and list for new directions.
        Random random = new Random();
        List< Vector > newDirections = new ArrayList<>();

        /*
        Add up if the current direction is not down and it would not lead off the lawn based on
        the current position of the turn.
        */
        if( turn.y != 0.0f + CELL_NORMAL_HEIGHT * 5.0f )
            if( !currentDirection.equals( Vector.down ) )
                newDirections.add( Vector.up );

        // Remaining directions added similarly to the above.

        if( turn.y != 0.0f - CELL_NORMAL_HEIGHT * 5.0f )
            if( !currentDirection.equals( Vector.up ) )
                newDirections.add( Vector.down );

        if( turn.x != 0.0f - CELL_NORMAL_WIDTH * 3.0f )
            if( !currentDirection.equals( Vector.right ) )
                newDirections.add( Vector.left );

        if( turn.x != 0.0f + CELL_NORMAL_WIDTH * 3.0f )
            if( !currentDirection.equals( Vector.left ) )
                newDirections.add( Vector.right );

        // Return a random direction from the ones added to the list.
        return newDirections.get( random.nextInt( newDirections.size() ) );
    }

    // Animate the centipede through any turn it encountered on its heading.
    private void animateThroughTurns( Centipede centipede, Point nextPosition ) {
        // Check each turn to see if it was traversed.
        for( Point turn : TURNS ) {
            // Cache previous position for speedier access.
            Point previousPosition = centipede.getPosition();

            // Disregard turns that the centipede did not traverse.
            if( !turn.intersectsPathOf( previousPosition, nextPosition ) ) continue;

            // Guard against time slice issues where the centipede is at the previous frame's turn.
            if( centipede.getTurn() == turn ) break;

            // Otherwise, the centipede has traversed the turn.  Remember it.
            centipede.setTurn( turn );

            // Get the centipede's direction before changing it.
            Vector previousDirection = centipede.getDirection();

            // Heads get their own direction from the turn, while tails follow their heads.
            if( centipede.getIsHead() )
                centipede.setDirection( getNewDirectionForTurn( turn, centipede.getDirection() ) );
            else
                centipede.setDirection( centipede.getHead().getDirection() );

            // Get the centipede's direction after changing it.
            Vector direction = centipede.getDirection();

            // The centipede's previous direction matches the new one, keep it on course.
            if( centipede.getDirection() == previousDirection ) break;

            // No need to bend the centipede's path around the turn if its path ends dead on it.
            if( turn.equals( nextPosition ) ) break;

            // Handle the head case.
            if( centipede.getIsHead() ) {
                // Travel past the turn to be applied in the new direction after it.
                float travelAfterTurn;

                // Travel after turn is difference between X or Y axes depending on direction.
                if( turn.wasPassedVertically( previousPosition, nextPosition ) ) {
                    travelAfterTurn = Math.abs( nextPosition.y - turn.y );
                } else  {
                    travelAfterTurn = Math.abs( nextPosition.x - turn.x );
                }

                // Change the next position based on travel after turn in new direction.
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

            // Handle the tail case, based on the head's position.
            Point headPosition = centipede.getHead().getPosition();

            // Change the position based on the head's distance already past the turn.
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

        // Set the centipede's new position.
        centipede.setPosition( nextPosition );
    }
}
