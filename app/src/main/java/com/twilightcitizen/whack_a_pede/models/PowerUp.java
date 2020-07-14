/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.geometry.Vector;

import java.util.Random;

/*
PowerUp tracks various data necessary for the proper placement of a power up  on the lawn,
including position and direction along the X and Y axis, and last turns or holes reached.
*/
public class PowerUp {
    // Kinds of PowerUps available in the game.
    public enum Kind {
        plus1kPoints, plus10kPoints, plus100kPoints, slowDown;

        // Get a random power up kind the given random number generator.
        public static Kind getRandomKind( Random random ) {
            int selection = random.nextInt( 100 );

            if( selection < 50 ) return plus1kPoints; else
            if( selection < 75 ) return plus10kPoints; else
            if( selection < 90 ) return plus100kPoints; else return slowDown;
        }
    }

    // Kind of power up.
    private final Kind kind;
    // Current position.
    private Point position;
    // Direction of heading to be applied to speed of traversal.
    private Vector direction;
    // Direction of heading once turn is encountered.
    private Vector nextDirection;

    // PowerUp must have position and direction.
    public PowerUp( Point position, Vector direction, Kind kind ) {
        this.position = position;
        this.direction = direction;
        this.nextDirection = direction;
        this.kind = kind;
    }

    // Get the kind of power up.
    public Kind getKind() { return kind; }

    // Get or set the current position.
    public Point getPosition() { return position; }
    public void setPosition( Point position ) { this.position = position; }

    // Get or set the direction of traversal.
    public Vector getDirection() { return direction; }
    public void setDirection( Vector direction ) { this.direction = direction; }
    public Vector getNextDirection() { return nextDirection; }
    public void setNextDirection( Vector nextDirection ) { this.nextDirection = nextDirection; }
}