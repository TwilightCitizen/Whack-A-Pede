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
import com.twilightcitizen.whack_a_pede.models.PowerUp;
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
    // Random number generator used in various routines.
    private static final Random random = new Random();

    // Tag for filtering any debug message logged.
    private static final String TAG = "GameViewModel";

    // States that the game can be in.
    public enum State { newGame, paused, running, gameOver }

    // States for the game sync to leaderboard.
    public enum Sync { notSynced, syncing, synced, errorSyncing, nothingToSync }

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
    public static final float CENTIPEDE_NORMAL_WIDTH =  CELL_NORMAL_WIDTH * 0.8f;
    public static final float CENTIPEDE_NORMAL_HEIGHT = CELL_NORMAL_HEIGHT * 0.8f;

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
    public static final float CENTIPEDE_START_SPEED = CELL_NORMAL_WIDTH * 3.0f;
    public static final float CENTIPEDE_MAX_SPEED = CELL_NORMAL_WIDTH * 10.0f;

    // This more or less determines the number of rounds before max speed is reached.
    private static final float CENTIPEDE_SPEED_INCREASE =
        ( CENTIPEDE_MAX_SPEED - CENTIPEDE_START_SPEED ) / 1_000.0f;

    // Edges of the lawn where centipedes can enter it.
    private enum StartingEdge {
        top, bottom, left, right;

        // Get a random starting edge using the given random number generator.
        public static StartingEdge getRandomStartingEdge() {
            return StartingEdge.values()[ random.nextInt( StartingEdge.values().length ) ];
        }

        // Get a point along the current starting edge using the given random number generator.
        public Point getRandomPointAlongEdge() {
            switch( this ) {
                case top:
                    return new Point( TURNS_X[ random.nextInt( TURNS_X.length ) ], 1.0f );
                case bottom:
                    return new Point( TURNS_X[ random.nextInt( TURNS_X.length ) ], -1.0f );
                case left:
                    return new Point( -LAWN_CELLS_RATIO, TURNS_Y[ random.nextInt( TURNS_Y.length ) ] );
                default:
                    return new Point( LAWN_CELLS_RATIO, TURNS_Y[ random.nextInt( TURNS_Y.length ) ] );
            }
        }

        // Get the direction into the lawn from current edge.
        public Vector getDirectionIntoEdge() {
            switch( this ) {
                case top: return Vector.down;
                case bottom: return Vector.up;
                case left: return Vector.right;
                default: return Vector.left;
            }
        }
    }

    // Centipedes on the lawn at any given time.
    public static final List< Centipede > CENTIPEDES = new ArrayList<>();

    // Power ups on the lawn at any given time.
    public static final List< PowerUp > POWER_UPS = new ArrayList<>();

    // The number of segments in a fresh centipede.
    public static final int segmentsPerCentipede = 10;

    // Null coalesce mutable live data as a value.
    public static <T> T getNullCoalescedValue( MutableLiveData< T > mutableLiveData, T fallback ) {
        T value = mutableLiveData.getValue();

        return value == null ? fallback : value;
    }

    // Current speed of centipedes on the lawn.
    private final MutableLiveData< Float > centipedeSpeed =
        new MutableLiveData<>( CENTIPEDE_START_SPEED );

    // Expose the mutable live data for speed.
    public MutableLiveData< Float > getCentipedeSpeed() { return centipedeSpeed; }
    public void refreshCentipedeSpeed() { centipedeSpeed.postValue( centipedeSpeed.getValue() ); }

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

    // Power ups must also be managed with concurrent list manipulation in mind.
    final ArrayList< PowerUp > powerUpsToRemove = new ArrayList<>();

    // Number of seconds to wait between adding power ups.
    public static final int SECONDS_BETWEEN_POWER_UPS = 60;

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

    private final MutableLiveData< Sync > leaderboardSync = new MutableLiveData<>( Sync.notSynced );

    // Expose the mutable live data for score, rounds, time remaining, and elapsed time.
    public MutableLiveData< Integer > getScore() { return score; }
    public MutableLiveData< Integer > getRounds() { return rounds; }
    public MutableLiveData< Long > getRemainingTimeMillis() { return remainingTimeMillis; }
    public MutableLiveData< Long > getElapsedTimeMillis() { return elapsedTimeMillis; }
    public MutableLiveData< Sync > getLeaderboardSync() { return leaderboardSync; }
    public void setSyncedToLeaderboard( Sync sync ) { leaderboardSync.setValue( sync ); }

    /*
    Mutable live data for the game's overall state allows external observers to update as needed
    whenever the game's state changes.
    */
    private final MutableLiveData< State > state = new MutableLiveData<>( State.newGame );

    // Expose the mutable live data game state.
    public MutableLiveData< State > getState() { return state; }

    // Achievement unlock flags.
    private boolean headWhack = false;
    private boolean tailsOnly = false;
    private boolean doubleElimination = false;
    private boolean tripleElimination = false;
    private boolean quadrupleElimination = false;
    private boolean halfLife = false;

    // Accumulators for some achievement unlock flags.
    private int headsTapped = 0;
    private int tailsTapped = 0;

    // Expose achievement unlock flags.
    public boolean getHeadWhack() { return headWhack; }
    public boolean getTailsOnly() { return tailsOnly; }
    public boolean getDoubleElimination() { return doubleElimination; }
    public boolean getTripleElimination() { return tripleElimination; }
    public boolean getQuadrupleElimination() { return quadrupleElimination; }
    public boolean getHalfLife() { return halfLife; }

    // Number of power up opportunities passed.
    private int powerUpOpportunities = 0;

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

    // Reset all values to where they should be for a fresh game.
    private void setupNewGame() {
        CENTIPEDES.clear();
        POWER_UPS.clear();
        state.setValue( State.newGame );
        centipedeSpeed.setValue( CENTIPEDE_START_SPEED );
        score.setValue( STARTING_SCORE );
        rounds.setValue( STARTING_ROUNDS );
        remainingTimeMillis.setValue( STARTING_REMAINING_TIME_MILLIS );
        elapsedTimeMillis.setValue( STARTING_ELAPSED_TIME_MILLIS );
        leaderboardSync.setValue( Sync.notSynced );

        headWhack = false;
        tailsOnly = false;
        doubleElimination = false;
        tripleElimination = false;
        quadrupleElimination = false;
        halfLife = false;
        headsTapped = 0;
        tailsTapped = 0;
        powerUpOpportunities = 0;
    }

    // Loop the game through the provided time slice.
    public void loop( long elapsedTimeMillis  ) {
        // Guard against changing anything if the game state is not running.
        if( state.getValue() != State.running ) return;

        // Obtain elapsed time and time remaining values.
        Long timeElapsedValue =  getNullCoalescedValue( this.elapsedTimeMillis, 0L );
        Long timeRemainingValue = getNullCoalescedValue( remainingTimeMillis, 0L );

        // Update the time elapsed and time remaining with the provided slice.
        this.elapsedTimeMillis.postValue( timeElapsedValue + elapsedTimeMillis );
        remainingTimeMillis.postValue( timeRemainingValue - elapsedTimeMillis );

        // Add power ups, check for game over or next round, then attack and animate centipedes an power ups.
        addPowerUps();
        checkForGameOver();
        checkForNextRound();
        attackPowerUps();
        attackCentipedes();
        animatePowerUps( elapsedTimeMillis );
        animateCentipedes( elapsedTimeMillis );
    }

    // Add power ups to the lawn based on elapsed time and chance.
    private void addPowerUps() {
        Long timeElapsedValue = getNullCoalescedValue( this.elapsedTimeMillis, 0L );

        int powerUpOpportunities = (int) (
            timeElapsedValue / TimeUtil.secondsToMillis( SECONDS_BETWEEN_POWER_UPS )
        );

        if( this.powerUpOpportunities == powerUpOpportunities ) return;

        this.powerUpOpportunities = powerUpOpportunities;

        setupPowerUp();
    }

    /*
    Process collected touch events as power up attacks.  Remove touched power ups, if any, from the
    lawn, applying the power ups to the game according to their desired effect.
    */
    private void attackPowerUps() {
        // Guard against processing attacks if there are no touch points or no power ups.
        if( touchPoints.isEmpty() || POWER_UPS.isEmpty() ) return;

        killTouchedPowerUps();
        applyKilledPowerUps();

        // Do the actual removes.
        POWER_UPS.removeAll( powerUpsToRemove );

        /*
        Play an appropriate sound for a power up hit.  Ignore misses to prevent playing a double-miss
        sound, one for power ups and one for centipede segments.
        */
        if( !powerUpsToRemove.isEmpty() ) SoundUtil.playPowerUp();

        // Clear power up attack list to avoid needless checks.
        powerUpsToRemove.clear();
    }

    // Apply the effect of the killed power ups to the game.
    private void applyKilledPowerUps() {
        int scoreValue = getNullCoalescedValue( score, 0 );

        for( PowerUp powerUp : powerUpsToRemove ) {
            switch( powerUp.getKind() ) {
                case plus1kPoints: score.postValue( scoreValue + 1_000 ); Log.wtf( "POWER UP", "1K" ); break;
                case plus10kPoints: score.postValue( scoreValue + 10_000 ); Log.wtf( "POWER UP", "10K" ); break;
                case plus100kPoints: score.postValue( scoreValue + 100_000 ); Log.wtf( "POWER UP", "100K" ); break;
                case slowDown: centipedeSpeed.postValue( CENTIPEDE_START_SPEED ); Log.wtf( "POWER UP", "SLOW DOWN" );
            }
        }
    }

    // Check all power ups against touches, killing ones that were touched.
    private void killTouchedPowerUps() {
        // Process all touch points as attacks.
        for( Point touchPoint : touchPoints ) {
            // Cache the X and Y of the touch event.
            float x = touchPoint.x; float y = touchPoint.y;

            if( LoggerUtil.DEBUGGING ) Log.wtf( TAG, "TOUCH - " + x + ", " + y );

            // Test attacks against all centipedes.
            for( PowerUp powerUp : POWER_UPS ) {
                // Cache the centipede position.
                Point position = powerUp.getPosition();

                if( LoggerUtil.DEBUGGING )
                    Log.wtf( TAG, "POWER UP - " + position.x + ", " + position.y );

                // Touch X falls within X bounds of centipede.
                boolean touchedX =
                    x >= position.x - CENTIPEDE_NORMAL_RADIUS &&
                    x <= position.x + CENTIPEDE_NORMAL_RADIUS;

                // Touch Y falls within Y bounds of centipede.
                boolean touchedY =
                    y >= position.y - CENTIPEDE_NORMAL_RADIUS &&
                    y <= position.y + CENTIPEDE_NORMAL_RADIUS;

                // Touch falls in bounds of centipede and centipede is above ground.
                boolean touched = touchedX && touchedY;

                // Add any touched segments to the killed segments collection.
                if( touched ) {
                    if( LoggerUtil.DEBUGGING ) Log.wtf( TAG, "TOUCH ON POWER UP" );

                    powerUpsToRemove.add( powerUp );
                }
            }
        }
    }

    // Animate power ups over the provided time slice.
    private void animatePowerUps( long elapsedTimeMillis ) {
        // Normalize the elapsed time as a fraction of 1 second.
        float interval = TimeUtil.millisToIntervalOfSeconds( elapsedTimeMillis );

        // Obtain centipede speed value.
        Float powerUpSpeedValue = getNullCoalescedValue( centipedeSpeed, CENTIPEDE_START_SPEED );

        // Animate each power up.
        for( PowerUp powerUp : POWER_UPS ) {
            // Generate a new position for it based on current position, direction, and speed.
            Point nextPosition = new Point(
                powerUp.getPosition().x + powerUpSpeedValue * interval * powerUp.getDirection().x,
                powerUp.getPosition().y + powerUpSpeedValue * interval * powerUp.getDirection().y
            );

            /*
             Get new directions from turns at approach, change trajectory through them, and
             rotate smoothing around them.
            */
            // Cache previous position for speedier access.
            Point previousPosition = powerUp.getPosition();
            // Get the power up's direction before changing it.
            Vector previousDirection = powerUp.getDirection();

            // Check each turn to see if it was traversed.
            for( Point turn : TURNS ) {
                // Disregard turns that the centipede did not traverse.
                if( !turn.intersectsPathOf( previousPosition, nextPosition ) ) continue;

                powerUp.setNextDirection( getNewDirectionForTurn( turn, previousDirection ) );

                // Use new direction gotten from turn at approach.
                powerUp.setDirection( powerUp.getNextDirection() );

                // Get the centipede's direction after changing it.
                Vector nextDirection = powerUp.getDirection();

                // The centipede's previous direction matches the new one, keep it on course.
                if( nextDirection == previousDirection ) break;

                // No need to bend the centipede's path around the turn if its path ends dead on it.
                if( turn.equals( nextPosition ) ) break;

                // Travel past the turn to be applied in the new direction after it.
                float travelAfterTurn;

                // Travel after turn is difference between X or Y axes depending on direction.
                if( turn.wasPassedVertically( previousPosition, nextPosition ) ) {
                    travelAfterTurn = Math.abs( nextPosition.y - turn.y );
                } else  {
                    travelAfterTurn = Math.abs( nextPosition.x - turn.x );
                }

                // Change the next position based on travel after turn in new direction.
                if( nextDirection.equals( Vector.up ) ) {
                    nextPosition = new Point( turn.x, nextPosition.y + travelAfterTurn );
                } else if( nextDirection.equals( Vector.down ) ) {
                    nextPosition = new Point( turn.x, nextPosition.y - travelAfterTurn );
                } else if( nextDirection.equals( Vector.left ) ) {
                    nextPosition = new Point( nextPosition.x - travelAfterTurn, turn.y );
                } else {
                    nextPosition = new Point( nextPosition.x + travelAfterTurn, turn.y );
                }

                break;
            }

            // Set the centipede's new position.
            powerUp.setPosition( nextPosition );
        }
    }

    // Setup a new power up to enter the lawn from a random location at one of its edges.
    private void setupPowerUp() {
        // Pick an edge of the lawn at random.
        StartingEdge startingEdge = StartingEdge.getRandomStartingEdge();
        // Position and direction for the new centipede.
        Point startingPosition = startingEdge.getRandomPointAlongEdge();
        Vector startingDirection = startingEdge.getDirectionIntoEdge();
        // Generate the kind of power up randomly.
        PowerUp.Kind kind = PowerUp.Kind.getRandomKind( random );
        // Create a new power up with the random position and direction picked.
        PowerUp powerUp = new PowerUp( startingPosition, startingDirection, kind );

        POWER_UPS.add( powerUp );
    }

    // Setup a new centipede to enter the lawn from a random location at one of its edges.
    private void setupCentipede() {
        // Guard against adding a new centipede when one already exists.
        if( !CENTIPEDES.isEmpty() ) return;

        // Pick an edge of the lawn at random.
        StartingEdge startingEdge = StartingEdge.getRandomStartingEdge();
        // Position and direction for the new centipede.
        Point startingPosition = startingEdge.getRandomPointAlongEdge();
        Vector startingDirection = startingEdge.getDirectionIntoEdge();
        // Create a new centipede with the random position and opposing direction picked.
        Centipede centipede = new Centipede( startingPosition, startingDirection );

        // Randomize its above/below ground position.
        if( ( random.nextInt( 2 ) + 1 ) % 2 == 0 ) centipede.toggleAbove();

        // Give it tails and add it to the lawn.
        centipede.addTails( segmentsPerCentipede - 1 );
        CENTIPEDES.add( centipede );

        headsTapped = 0;
        tailsTapped = 0;
    }

    // Check for game over conditions and switch state as needed.
    private void checkForGameOver() {
        // Obtain time remaining value.
        Long timeRemainingValue = getNullCoalescedValue( remainingTimeMillis, 0L );

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

        // Obtain score, rounds, time remaining, and centipede speed values.
        Integer scoreValue = getNullCoalescedValue( score, 0 );
        Integer roundsValue = getNullCoalescedValue( rounds, 1 );
        Long timeRemainingValue = getNullCoalescedValue( remainingTimeMillis, 0L );
        Float centipedeSpeedValue = getNullCoalescedValue( centipedeSpeed, CENTIPEDE_START_SPEED );

        // Add points for time remaining.
        score.postValue(
            scoreValue + TimeUtil.millisToSeconds( timeRemainingValue ) * BONUS_POINTS_PER_SECOND
        );

        // Increment rounds.
        rounds.postValue( roundsValue + 1 );

        // Reset the clock.
        remainingTimeMillis.postValue( ROUND_TIME_MILLIS );

        // Set the next round's centipede starting speed.
        float newSpeed = centipedeSpeedValue + CENTIPEDE_SPEED_INCREASE;
        newSpeed = Math.min( newSpeed, CENTIPEDE_MAX_SPEED );

        centipedeSpeed.postValue( newSpeed );

        // Setup a new centipede and play an appropriate sound.
        SoundUtil.playNewRound();
        applyRoundAchievements();
        setupCentipede();
    }

    // Unlock any achievements for the game tracked within in the round.
    private void applyRoundAchievements() {
        headWhack |= headsTapped == segmentsPerCentipede;
        tailsOnly |= tailsTapped == segmentsPerCentipede - 1;
    }

    /*
    Process collected touch events as centipede attacks.  Remove touched segments of the centipedes,
    if any, from the lawn, splitting or scattering attached centipedes as needed, and speeding them
    all up.  Score points for the player for all successfully attacked segments.
    */
    private void attackCentipedes() {
        // Guard against processing attacks if there are no touch points.
        if( touchPoints.isEmpty() ) return;

        killTouchedCentipedes();
        reorganizeCentipedes();

        // Do the actual adds and removes.
        CENTIPEDES.removeAll( centipedesToRemove );
        CENTIPEDES.addAll( centipedesToAdd );

        // Update the scoreboard.
        postScoreAndTimeBonuses();

        // Obtain centipede speed value.
        Float centipedeSpeedValue = getNullCoalescedValue( centipedeSpeed, CENTIPEDE_START_SPEED );

        // Figure out the new speed for all segments.
        centipedeSpeed.postValue( centipedeSpeedValue + CENTIPEDE_SPEED_INCREASE * centipedesKilled.size() );

        // Play an appropriate sound.
        if( centipedesKilled.isEmpty() ) SoundUtil.playMiss(); else SoundUtil.playHit();

        // Clear touch points so they cannot keep killing centipedes.
        touchPoints.clear();

        // Process multiple elimination achievements.
        int eliminationCount = centipedesKilled.size();

        if( eliminationCount >= 5 ) {
            doubleElimination = tripleElimination = quadrupleElimination = halfLife = true;
        } if( eliminationCount == 4 ) {
            doubleElimination = tripleElimination = quadrupleElimination = true;
        } if( eliminationCount == 3 ) {
            doubleElimination = tripleElimination = true;
        } if( eliminationCount == 2 ) {
            doubleElimination = true;
        }

        // Clear centipede attack lists to avoid needless checks.
        centipedesKilled.clear();
        centipedesToRemove.clear();
        centipedesToAdd.clear();
    }

    // Update the score and time remaining with points and/or time bonuses.
    private void postScoreAndTimeBonuses() {
        // Obtain score and time remaining values.
        Integer scoreValue = getNullCoalescedValue( score, 0 );
        Long timeRemainingValue = getNullCoalescedValue( remainingTimeMillis, 0L );

        // Add points for each segment killed to the player's score.
        score.postValue( scoreValue + centipedesKilled.size() * POINTS_PER_CENTIPEDE );

        remainingTimeMillis.postValue(
            timeRemainingValue + centipedesKilled.size() * BONUS_MILLIS_PER_CENTIPEDE
        );
    }

    // Marked killed centipedes for removal and split or scatter remaining ones as needed.
    private void reorganizeCentipedes() {
        // Remove killed centipedes, splitting or scattering others as needed.
        for( Centipede centipede : centipedesKilled ) {
            // Killed heads are removed and all their tails are scattered as independent heads.
            if( centipede.getIsHead() ) {
                headsTapped++;
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
                tailsTapped++;

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
    }

    // Check all centipedes against touches, killing ones that were touched.
    private void killTouchedCentipedes() {
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
    }

    // Animate centipedes over the provided time slice.
    private void animateCentipedes( long elapsedTimeMillis ) {
        // Normalize the elapsed time as a fraction of 1 second.
        float interval = TimeUtil.millisToIntervalOfSeconds( elapsedTimeMillis );

        // Obtain centipede speed value.
        Float centipedeSpeedValue = getNullCoalescedValue( centipedeSpeed, CENTIPEDE_START_SPEED );

        // Animate each centipede, including its tails and their tails.
        for( Centipede centipede : CENTIPEDES ) while( centipede != null ) {
            // Generate a new position for it based on current position, direction, and speed.
            Point nextPosition = new Point(
                centipede.getPosition().x + centipedeSpeedValue * interval * centipede.getDirection().x,
                centipede.getPosition().y + centipedeSpeedValue * interval * centipede.getDirection().y
            );

            // Animate through holes to change above/below-ground layer.
            animateThroughHoles( centipede, nextPosition );
            /*
             Get new directions from turns at approach, change trajectory through them, and
             rotate smoothing around them.
            */
            approachTurns( centipede, nextPosition );
            animateThroughTurns( centipede, nextPosition );
            rotateAroundTurns( centipede, interval );

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

    // Smoothly rotate the centipede as it navigates turns.
    private void rotateAroundTurns( Centipede centipede, float interval ) {
        // Get its rotation percentage and the factor to add or subtract from it for the time slice.
        float rotationPercentage = centipede.getRotationPercentage();
        float rotationFactor = 1.0f / CELL_NORMAL_WIDTH * interval;

        // Increase or decrease the rotation percentage by the factor based on rotation trajectory.
        if( rotationPercentage > 0.0f && rotationPercentage <= 1.0f )
            centipede.setRotationPercentage( rotationPercentage - rotationFactor );
        else if( rotationPercentage < 0.0f && rotationPercentage >= -1.0f )
            centipede.setRotationPercentage( rotationPercentage + rotationFactor );

        // Rotate it by it target rotation less the percentage 90 degrees for the time slice.
        centipede.setRotation( centipede.getTargetRotation() - 90.0f * rotationPercentage );
    }

    // Get a new direction from a turn as the centipede approaches it so it can begin rotation.
    private void approachTurns( Centipede centipede, Point nextPosition ) {
        // Cache previous position for speedier access.
        Point previousPosition = centipede.getPosition();
        // Cache direction for speedier access.
        Vector direction = centipede.getDirection();
        // Point of approach is ahead of centipede center.
        Point previousApproach;
        Point nextApproach;

        // Find the approach or leading edge based on the centipede's current direction.
        if( direction == Vector.down ) {
            previousApproach = new Point( previousPosition.x, previousPosition.y - CELL_NORMAL_RADIUS );
            nextApproach = new Point( nextPosition.x, nextPosition.y - CELL_NORMAL_RADIUS );
        } else if( direction == Vector.up ) {
            previousApproach = new Point( previousPosition.x, previousPosition.y + CELL_NORMAL_RADIUS );
            nextApproach = new Point( nextPosition.x, nextPosition.y + CELL_NORMAL_RADIUS );
        } else if( direction == Vector.left ) {
            previousApproach = new Point( previousPosition.x - CELL_NORMAL_RADIUS, previousPosition.y );
            nextApproach = new Point( nextPosition.x - CELL_NORMAL_RADIUS, nextPosition.y );
        } else {
            previousApproach = new Point( previousPosition.x + CELL_NORMAL_RADIUS, previousPosition.y );
            nextApproach = new Point( nextPosition.x + CELL_NORMAL_RADIUS, nextPosition.y );
        }

        // Check each turn to see if it was traversed.
        for( Point turn : TURNS ) {
            // Disregard turns that the centipede did not approach.
            if( !turn.intersectsPathOf( previousApproach, nextApproach ) ) continue;

            // Heads get their own direction from the turn, while tails follow their heads.
            if( centipede.getIsHead() )
                centipede.setNextDirection( getNewDirectionForTurn( turn, direction ) );
            else
                centipede.setNextDirection( centipede.getHead().getNextDirection() );

            // Get the centipede's next direction after changing it.
            Vector nextDirection = centipede.getNextDirection();

            // Default not to rotate in case heading remains the same.
            centipede.setRotationPercentage( 0.0f );

            // The centipede's current direction matches the new one, no need to rotate.
            if( nextDirection == direction ) break;

            // Otherwise, set it up to start rotating for the turn.
            if( nextDirection == Vector.down ) {
                if( direction == Vector.left )
                    centipede.setRotationPercentage( 1.0f );
                else
                    centipede.setRotationPercentage( -1.0f );
            } else if( nextDirection == Vector.up ) {
                if( direction == Vector.left )
                    centipede.setRotationPercentage( -1.0f );
                else
                    centipede.setRotationPercentage( 1.0f );
            } else if( nextDirection == Vector.left ) {
                if( direction == Vector.down )
                    centipede.setRotationPercentage( -1.0f );
                else
                    centipede.setRotationPercentage( 1.0f );
            } else {
                if( direction == Vector.down )
                    centipede.setRotationPercentage( 1.0f );
                else
                    centipede.setRotationPercentage( -1.0f );
            }
        }
    }

    // Change the centipede's trajectory through any turn it encountered on its heading.
    private void animateThroughTurns( Centipede centipede, Point nextPosition ) {
        // Cache previous position for speedier access.
        Point previousPosition = centipede.getPosition();
        // Get the centipede's direction before changing it.
        Vector previousDirection = centipede.getDirection();

        // Check each turn to see if it was traversed.
        for( Point turn : TURNS ) {
            // Disregard turns that the centipede did not traverse.
            if( !turn.intersectsPathOf( previousPosition, nextPosition ) ) continue;

            // Use new direction gotten from turn at approach.
            centipede.setDirection( centipede.getNextDirection() );

            // Get the centipede's direction after changing it.
            Vector nextDirection = centipede.getDirection();

            // The centipede's previous direction matches the new one, keep it on course.
            if( nextDirection == previousDirection ) break;

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
                if( nextDirection.equals( Vector.up ) ) {
                    nextPosition = new Point( turn.x, nextPosition.y + travelAfterTurn );
                } else if( nextDirection.equals( Vector.down ) ) {
                    nextPosition = new Point( turn.x, nextPosition.y - travelAfterTurn );
                } else if( nextDirection.equals( Vector.left ) ) {
                    nextPosition = new Point( nextPosition.x - travelAfterTurn, turn.y );
                } else {
                    nextPosition = new Point( nextPosition.x + travelAfterTurn, turn.y );
                }

                break;
            }

            // Handle the tail case, based on the head's position.
            Point headPosition = centipede.getHead().getPosition();

            // Change the position based on the head's distance already past the turn.
            if( nextDirection.equals( Vector.up ) ) {
                nextPosition = new Point( turn.x, headPosition.y - CENTIPEDE_NORMAL_HEIGHT );
            } else if( nextDirection.equals( Vector.down ) ) {
                nextPosition = new Point( turn.x, headPosition.y + CENTIPEDE_NORMAL_HEIGHT );
            } else if( nextDirection.equals( Vector.left ) ) {
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
