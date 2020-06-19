/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.viewModels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.geometry.Vector;
import com.twilightcitizen.whack_a_pede.models.Centipede;
import com.twilightcitizen.whack_a_pede.utilities.LoggerUtil;
import com.twilightcitizen.whack_a_pede.utilities.SoundUtil;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
Game ViewModel abstracts the necessary details of an ongoing game of Whack-A-Pede within a module
that can survive lifecycle events of the activity to which it belongs.
*/
public class GameViewModel extends ViewModel {
    // Tag for filtering any debug message logged.
    private static final String TAG = "GameViewModel";

    // States that the game can be in.
    public enum State { newGame, paused, running, gameOver }

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

    public static final float CENTIPEDE_NORMAL_RADIUS = CELL_NORMAL_RADIUS * 0.8f;
    public static final float CENTIPEDE_NORMAL_WIDTH =  CELL_NORMAL_WIDTH * 0.8f; //CENTIPEDE_NORMAL_RADIUS * 2.0f;
    public static final float CENTIPEDE_NORMAL_HEIGHT = CELL_NORMAL_HEIGHT * 0.8f; // CENTIPEDE_NORMAL_RADIUS * 2.0f;

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
    private static final int POINTS_PER_CENTIPEDE = 100;
    private static final long BONUS_MILLIS_PER_CENTIPEDE = ROUND_TIME_MILLIS;
    private static final int BONUS_POINTS_PER_SECOND = 10;
    private static final int STARTING_SCORE = 0;
    private static final int STARTING_ROUNDS = 1;
    private static final long STARTING_REMAINING_TIME_MILLIS = ROUND_TIME_MILLIS;
    private static final long STARTING_ELAPSED_TIME_MILLIS = 0L;

    // Centipede speed constants.
    private static final float CENTIPEDE_START_SPEED = CELL_NORMAL_WIDTH * 3.0f;
    private static final float CENTIPEDE_MAX_SPEED = CELL_NORMAL_WIDTH * 12.0f;

    // This more or less determines the number of rounds before max speed is reached.
    private static final float CENTIPEDE_SPEED_INCREASE =
        ( CENTIPEDE_MAX_SPEED - CENTIPEDE_START_SPEED ) / 1_000.0f;

    // Edges of the lawn where centipedes can enter it.
    private enum StartingEdge { top, bottom, left, right }

    // Centipedes on the lawn at any given time.
    public static final List< Centipede > CENTIPEDES = new ArrayList<>();

    // Current speed of centipedes on the lawn.
    private float centipedeSpeed = CENTIPEDE_START_SPEED;

    // Touch events received from the surface to which the game is drawn.
    private final ArrayList< Point > touchPoints = new ArrayList<>();

    // Expose setter to add touch events.
    public void addTouchPoint( Point touchPoint ) {
        if( state.getValue() == State.running ) touchPoints.add( touchPoint );
    }

    // Attacked centipedes must be managed carefully to avoid concurrent list manipulation.
    final ArrayList< Centipede > centipedesKilled = new ArrayList<>();
    final ArrayList< Centipede > centipedesToRemove = new ArrayList<>();
    final ArrayList< Centipede > centipedesToAdd = new ArrayList<>();

    /*
    Mutable live data for scoring and timing information allows external observers to update
    as needed whenever these values change.
    */
    private final MutableLiveData< Integer > score = new MutableLiveData<>( STARTING_SCORE );
    private final MutableLiveData< Integer > rounds = new MutableLiveData<>( STARTING_ROUNDS );

    private final MutableLiveData< Long > remainingTimeMillis =
        new MutableLiveData<>( STARTING_REMAINING_TIME_MILLIS );

    private final MutableLiveData< Long > elapsedTimeMillis =
        new MutableLiveData<>( STARTING_ELAPSED_TIME_MILLIS );

    // Expose the mutable live data for score, rounds, time remaining, and elapsed time.
    public MutableLiveData< Integer > getScore() { return score; }
    public MutableLiveData< Integer > getRounds() { return rounds; }
    public MutableLiveData< Long > getRemainingTimeMillis() { return remainingTimeMillis; }
    public MutableLiveData< Long > getElapsedTimeMillis() { return elapsedTimeMillis; }

    /*
    Mutable live data for the game's overall state allows external observers to update as needed
    whenever the game's state changes.
    */
    private final MutableLiveData< State > state = new MutableLiveData<>( State.newGame );

    // Expose the mutable live data game state.
    public MutableLiveData< State > getState() { return state; }

    /*
    The following methods allow external game observers to change the game's state as needed.  Calls
    that would move the game between states in an invalid or unexpected way with throw an exception.
    */

    // Only stopped games should be reset to new games.
    public void reset() {
        if( state.getValue() != State.gameOver )
            throw new IllegalStateException( "Game Reset when Not Game Over" );

        setupNewGame();
    }

    // Only running games should be paused.  Other states basically assumed quasi-paused state.
    public void pause() {
        if( state.getValue() != State.running ) return;

        SoundUtil.pauseMusic();
        state.setValue( State.paused );
    }

    // Only new or paused games can be started or resumed.
    public void play() {
        if( !( state.getValue() == State.paused || state.getValue() == State.newGame ) )
            throw new IllegalStateException( "Game Played while Not Paused or New" );

        setupCentipede();
        SoundUtil.playMusic();
        state.setValue( State.running );
    }

    // Resume is semantically similar to play, but offers different accessibility feedback.
    public void resume() { play(); }

    // A running game should be paused first.  Otherwise, not yet started to stop or already stopped.
    public void quit() {
        if( state.getValue() != State.paused )
            throw new IllegalStateException( "Game Stopped while Not Paused" );

        SoundUtil.stopMusic();
        setupNewGame();
    }

    private void setupNewGame() {
        centipedeSpeed = CENTIPEDE_START_SPEED;

        CENTIPEDES.clear();
        state.setValue( State.newGame );
        score.setValue( STARTING_SCORE );
        rounds.setValue( STARTING_ROUNDS );
        remainingTimeMillis.setValue( STARTING_REMAINING_TIME_MILLIS );
        elapsedTimeMillis.setValue( STARTING_ELAPSED_TIME_MILLIS );
    }

    // Loop the game through the provided time slice.
    public void loop( long elapsedTimeMillis  ) {
        // Guard against changing anything if the game state is not running.
        if( state.getValue() != State.running ) return;

        // Obtain elapsed time and time remaining values.
        Long timeElapsedValue = this.elapsedTimeMillis.getValue();
        Long timeRemainingValue = remainingTimeMillis.getValue();

        // Null check and coalesce elapsed time and time remaining values.
        timeElapsedValue = timeElapsedValue == null ? 0 : timeElapsedValue;
        timeRemainingValue = timeRemainingValue == null ? 0 : timeRemainingValue;

        // Update the time elapsed and time remaining with the provided slice.
        this.elapsedTimeMillis.postValue( timeElapsedValue + elapsedTimeMillis );
        remainingTimeMillis.postValue( timeRemainingValue - elapsedTimeMillis );

        // Check for game over or next round, then attack and animate centipedes.
        checkForGameOver();
        checkForNextRound();
        attackCentipedes();
        animateCentipedes( elapsedTimeMillis );
    }

    // Setup a new centipede to enter the lawn from a random location at one of its edges.
    private void setupCentipede() {
        // Guard against adding a new centipede when one already exists.
        if( !CENTIPEDES.isEmpty() ) return;

        // Position and direction for the new centipede.
        Point startingPosition = null;
        Vector startingDirection = null;

        // Random numbers for selecting lawn edges and positions along it.
        Random random = new Random();

        // Pick an edge of the lawn at random.
        StartingEdge startingEdge = StartingEdge.values()[
            random.nextInt( StartingEdge.values().length )
        ];

        // Pick a random position along that edge for the centipede with an opposing direction.
        switch( startingEdge ) {
            case top:
                startingPosition = new Point( TURNS_X[ random.nextInt( TURNS_X.length ) ], 1.0f );
                startingDirection = Vector.down;
                break;
            case bottom:
                startingPosition = new Point( TURNS_X[ random.nextInt( TURNS_X.length ) ], -1.0f );
                startingDirection = Vector.up;
                break;
            case left:
                startingPosition =
                    new Point( -LAWN_CELLS_RATIO, TURNS_Y[ random.nextInt( TURNS_Y.length ) ] );

                startingDirection = Vector.right;
                break;
            case right:
                startingPosition =
                    new Point( LAWN_CELLS_RATIO, TURNS_Y[ random.nextInt( TURNS_Y.length ) ] );

                startingDirection = Vector.left;
                break;
        }

        // Create a new centipede with the random position and opposing direction picked.
        Centipede centipede = new Centipede( startingPosition, startingDirection );

        // Randomize its above/below ground position.
        if( ( random.nextInt( 2 ) + 1 ) % 2 == 0 ) centipede.toggleAbove();

        // Give it 9 tails and add it to the lawn.
        centipede.addTails( 9 );
        CENTIPEDES.add( centipede );
    }

    // Check for game over conditions and switch state as needed.
    private void checkForGameOver() {
        // Obtain time remaining value.
        Long timeRemainingValue = remainingTimeMillis.getValue();

        // Null check and coalesce time remaining value.
        timeRemainingValue = timeRemainingValue == null ? 0 : timeRemainingValue;

        // Game is over if timer reaches zero.
        if( timeRemainingValue <= 0 ) {
            // Prevent negative clock.
            remainingTimeMillis.postValue( 0L );
            // Flag Game Over and pause it.
            state.postValue( State.gameOver );

            // Play an appropriate sound.
            SoundUtil.playGameOver();
            SoundUtil.stopMusic();
        }
    }

    // Check for new round conditions, modifying score and time and adding centipedes as needed.
    private void checkForNextRound() {
        // Guard against starting new round when centipedes still exist.
        if( !CENTIPEDES.isEmpty() ) return;

        // Obtain score, rounds, and time remaining values.
        Integer scoreValue = score.getValue();
        Integer roundsValue = rounds.getValue();
        Long timeRemainingValue = remainingTimeMillis.getValue();

        // Null check and coalesce score, rounds, and time remaining values.
        scoreValue = scoreValue == null ? 0 : scoreValue;
        roundsValue = roundsValue == null ? 1 : roundsValue;
        timeRemainingValue = timeRemainingValue == null ? 0 : timeRemainingValue;

        // Add points for time remaining.
        score.postValue(
            scoreValue + TimeUtil.millisToSeconds( timeRemainingValue ) * BONUS_POINTS_PER_SECOND
        );

        // Increment rounds.
        rounds.postValue( roundsValue + 1 );

        // Reset the clock.
        remainingTimeMillis.postValue( ROUND_TIME_MILLIS );

        // Set the next round's centipede starting speed.
        centipedeSpeed += CENTIPEDE_SPEED_INCREASE;
        centipedeSpeed = Math.min( centipedeSpeed, CENTIPEDE_MAX_SPEED );

        // Setup a new centipede and play an appropriate sound.
        SoundUtil.playNewRound();
        setupCentipede();
    }

    /*
    Process collected touch events as centipede attacks.  Remove touched segments of the centipedes,
    if any, from the lawn, splitting or scattering attached centipedes as needed, and speeding them
    all up.  Score points for the player for all successfully attacked segments.
    */
    private void attackCentipedes() {
        // Guard against processing attacks if there are no touch points.
        if( touchPoints.isEmpty() ) return;

        // Process all touch points as attacks.
        for( Point touchPoint : touchPoints ) {
            // Cache the X and Y of the touch event.
            float x = touchPoint.x; float y = touchPoint.y;

            if( LoggerUtil.DEBUGGING ) Log.wtf( TAG, "TOUCH - " + x + ", " + y );

            // Test attacks against all centipedes.
            for( Centipede centipede : CENTIPEDES ) while( centipede != null ) {
                // Cache the centipede position.
                Point position = centipede.getPosition();

                if( LoggerUtil.DEBUGGING )
                    Log.wtf( TAG, "CENTIPEDE - " + position.x + ", " + position.y );

                // Touch X falls within X bounds of centipede.
                boolean touchedX =
                    x >= position.x - CENTIPEDE_NORMAL_RADIUS &&
                    x <= position.x + CENTIPEDE_NORMAL_RADIUS;

                // Touch Y falls within Y bounds of centipede.
                boolean touchedY =
                    y >= position.y - CENTIPEDE_NORMAL_RADIUS &&
                    y <= position.y + CENTIPEDE_NORMAL_RADIUS;

                // Touch falls in bounds of centipede and centipede is above ground.
                boolean touched = touchedX && touchedY && centipede.getIsAbove();

                // Add any touched segments to the killed segments collection.
                if( touched ) {
                    if( LoggerUtil.DEBUGGING ) Log.wtf( TAG, "TOUCH ON CENTIPEDE" );

                    centipedesKilled.add( centipede );
                }

                centipede = centipede.getTail();
            }
        }

        // Remove killed centipedes, splitting or scattering others as needed.
        for( Centipede centipede : centipedesKilled ) {
            // Killed heads are removed and all their tails are scattered as independent heads.
            if( centipede.getIsHead() ) {
                Centipede tail = centipede.getTail();

                centipede.removeTail();
                centipedesToRemove.add( centipede );

                while( tail != null ) {
                    centipedesToAdd.add( tail );
                    tail.removeHead();

                    Centipede newTail = tail.getTail();

                    tail.removeTail();

                    tail = newTail;
                }
            // Killed tails just go away.
            } else if ( centipede.getIsTail() ) {
                centipede.getHead().removeTail();
                centipede.removeHead();
            // Killed middles split the centipede into two independent ones.
            } else {
                centipedesToAdd.add( centipede.getTail() );
                centipede.getHead().removeTail();
                centipede.removeHead();
                centipede.getTail().removeHead();
                centipede.removeTail();
            }
        }

        // Do the actual adds and removes.
        CENTIPEDES.removeAll( centipedesToRemove );
        CENTIPEDES.addAll( centipedesToAdd );

        // Obtain score and time remaining values.
        Integer scoreValue = score.getValue();
        Long timeRemainingValue = remainingTimeMillis.getValue();

        // Null check and coalesce score and time remaining values.
        scoreValue = scoreValue == null ? 0 : scoreValue;
        timeRemainingValue = timeRemainingValue == null ? 0 : timeRemainingValue;

        // Add points for each segment killed to the player's score.
        score.postValue( scoreValue + centipedesKilled.size() * POINTS_PER_CENTIPEDE );

        remainingTimeMillis.postValue(
            timeRemainingValue + centipedesKilled.size() * BONUS_MILLIS_PER_CENTIPEDE
        );

        // Figure out the new speed for all segments.
        centipedeSpeed += CENTIPEDE_SPEED_INCREASE * centipedesKilled.size();

        // Play an appropriate sound.
        if( centipedesKilled.isEmpty() ) SoundUtil.playMiss(); else SoundUtil.playHit();

        // Clear touch points so they cannot keep killing centipedes.
        touchPoints.clear();

        // Clear centipede attack lists to avoid needless checks.
        centipedesKilled.clear();
        centipedesToRemove.clear();
        centipedesToAdd.clear();
    }

    // Animate centipedes over the provided time slice.
    private void animateCentipedes( long elapsedTimeMillis ) {
        // Normalize the elapsed time as a fraction of 1 second.
        float interval = TimeUtil.millisToIntervalOfSeconds( elapsedTimeMillis );

        // Animate each centipede, including its tails and their tails.
        for( Centipede centipede : CENTIPEDES ) while( centipede != null ) {
            // Generate a new position for it based on current position, direction, and speed.
            Point nextPosition = new Point(
                centipede.getPosition().x + centipedeSpeed * interval * centipede.getDirection().x,
                centipede.getPosition().y + centipedeSpeed * interval * centipede.getDirection().y
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

            centipede.toggleAbove(); break;
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
                nextPosition = new Point( turn.x, headPosition.y - CENTIPEDE_NORMAL_HEIGHT );
            } else if( direction.equals( Vector.down ) ) {
                nextPosition = new Point( turn.x, headPosition.y + CENTIPEDE_NORMAL_HEIGHT );
            } else if( direction.equals( Vector.left ) ) {
                nextPosition = new Point( headPosition.x + CENTIPEDE_NORMAL_WIDTH, turn.y );
            } else {
                nextPosition = new Point( headPosition.x - CENTIPEDE_NORMAL_WIDTH, turn.y );
            }

            break;
        }

        // Set the centipede's new position.
        centipede.setPosition( nextPosition );
    }
}
