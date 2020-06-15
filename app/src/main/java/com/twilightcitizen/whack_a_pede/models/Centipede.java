/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.models;

import com.twilightcitizen.whack_a_pede.geometry.Point;
import com.twilightcitizen.whack_a_pede.geometry.Vector;

import static com.twilightcitizen.whack_a_pede.viewModels.GameViewModel.SEGMENT_NORMAL_RADIUS;

public class Centipede {
    private Centipede head = null;
    private Centipede tail = null;

    private boolean isAbove = true;

    private Point position;
    private Vector speed;
    private Vector direction;

    public Centipede( Point position, Vector speed, Vector direction ) {
        this.position = position; this.speed = speed; this.direction = direction;
    }

    public boolean getIsHead() { return head == null; }
    public boolean getIsTail() { return tail == null; }

    public Centipede getHead() { return head; }
    public Centipede getTail() { return tail; }
    public void removeHead() { head = null; }
    public void removeTail() { tail = null; }

    public boolean getIsAbove() { return isAbove; }
    public void toggleAbove() { isAbove = !isAbove; }

    public Point getPosition() { return position; }
    public void setPosition( Point position ) { this.position = position; }

    public Vector getSpeed() { return speed; }
    public void setSpeed( Vector speed ) { this.speed = speed; }

    public Vector getDirection() { return direction; }
    public void setDirection( Vector direction ) { this.direction = direction; }

    private Centipede addTail( Vector going ) {
        Centipede tail = new Centipede(
            new Point(
                position.x + SEGMENT_NORMAL_RADIUS * 2 * going.x,
                position.y + SEGMENT_NORMAL_RADIUS * 2 * going.y,
                0.0f
            ),

            speed, direction
        );

        this.tail = tail;
        tail.head = this;
        tail.isAbove = this.isAbove;

        return tail;
    }

    @SuppressWarnings( "SameParameterValue" ) public void addTails( int tails, Vector going ) {
        Centipede tail = this;

        while( tails > 0 ) {
            tail = tail.addTail( going );
            tails--;
        }
    }
}