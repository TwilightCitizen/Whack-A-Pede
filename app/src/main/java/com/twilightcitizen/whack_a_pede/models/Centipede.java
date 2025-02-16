/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.geometry.Vector;

import static com.twilightcitizen.whack_a_pede.viewModels.GameViewModel.CENTIPEDE_NORMAL_RADIUS;

/*
Centipede tracks various data necessary for the proper placement of a centipede  on the lawn,
including position and direction along the X and Y axis, position in the above- or below-ground
layers, last turns or holes reached, and their head and tail, if any.
*/
public class Centipede {
    // Head and tail, if any.
    private Centipede head = null;
    private Centipede tail = null;

    // Above/below-ground position, accomplished with layering since Z is assumed 0.0f.
    private boolean isAbove = true;

    // Current position.
    private Point position;
    // Direction of heading to be applied to speed of traversal.
    private Vector direction;
    // Direction of heading once turn is encountered.
    private Vector nextDirection;

    // Rotation and percentage for smooth rotation through turns.
    private Float rotation;
    private Float rotationPercentage = 0.0f;

    // Centipede must have position and direction.
    public Centipede( Point position, Vector direction ) {
        this.position = position;
        this.direction = direction;
        this.nextDirection = direction;
        this.rotation = direction.getTargetRotation();
    }

    // Determine if the centipede is a head or tail.  Can be both.
    public boolean getIsHead() { return head == null; }
    public boolean getIsTail() { return tail == null; }

    // Get or remove the head or tail.
    public Centipede getHead() { return head; }
    public Centipede getTail() { return tail; }
    public void removeHead() { head = null; }
    public void removeTail() { tail = null; }

    // Get or toggle the above/below-ground position.
    public boolean getIsAbove() { return isAbove; }
    public void toggleAbove() { isAbove = !isAbove; }

    // Get or set the current position.
    public Point getPosition() { return position; }
    public void setPosition( Point position ) { this.position = position; }

    // Get or set the direction of traversal.
    public Vector getDirection() { return direction; }
    public void setDirection( Vector direction ) { this.direction = direction; }
    public Vector getNextDirection() { return nextDirection; }
    public void setNextDirection( Vector nextDirection ) { this.nextDirection = nextDirection; }

    // Get or set the rotation and rotation related data.
    public float getRotation() { return rotation; }
    public void setRotation( float rotation ) { this.rotation = rotation; }
    public float getTargetRotation() { return nextDirection.getTargetRotation(); }
    public float getRotationPercentage() { return rotationPercentage; }
    public void setRotationPercentage( float rotationPercentage ) { this.rotationPercentage = rotationPercentage; }

    /*
    Add a tail to the centipede opposite side of its direction going in the same direction and
    at the same above/below-ground position.
    */
    private Centipede addTail() {
        Centipede tail = new Centipede(
            new Point(
                // Tail should be on opposite side of the centipede's direction.
                position.x + CENTIPEDE_NORMAL_RADIUS * 2.0f * ( 0.0f - direction.x ),
                position.y + CENTIPEDE_NORMAL_RADIUS * 2.0f * ( 0.0f - direction.y )
            ),

            // And, it should follow the this as its head.
            direction
        );

        // This centipede is the tail's head and the tail is this centipede's tail.
        this.tail = tail;
        tail.head = this;

        // It should also be on the same above/below-ground layer.
        tail.isAbove = this.isAbove;

        return tail;
    }

    // Add specified number of tails to this centipede, each successive tail the tail of the last.
    @SuppressWarnings( "SameParameterValue" ) public void addTails( int tails ) {
        // Start with this centipede.
        Centipede tail = this;

        // Add tails to this and successive tail centipedes.
        while( tails > 0 ) { tail = tail.addTail(); tails--; }
    }
}