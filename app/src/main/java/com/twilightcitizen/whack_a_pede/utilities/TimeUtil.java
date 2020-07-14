/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import java.util.Locale;

/*
Time Utility provides static utility methods the tracking and conversion of time .
*/
public class TimeUtil {
    // Last marked time, if any.
    private static Long lastMark = null;

    // Convert milliseconds into minutes and seconds as a string in the format of M:SS.
    public static String millisToMinutesAndSeconds( long millis ) {
        // Get the minutes and and seconds from millis.
        long minutes = ( millis / 1000 ) / 60;
        long seconds = ( millis / 1000 ) % 60;

        return String.format( Locale.getDefault(), "%d:%02d", minutes, seconds );
    }

    // Convert milliseconds into a fractional interval of seconds.
    public static float millisToIntervalOfSeconds( long millis ) { return millis / 1000.0f; }

    // Convert milliseconds to whole seconds.
    public static int millisToSeconds( long millis ) { return (int) millis / 60; }

    // Convert whole seconds to milliseconds.
    public static long secondsToMillis( int seconds ) { return (long) seconds * 1_000L; }

    // Mark current system time and return the elapsed time in milliseconds since last mark.
    public static long getTimeElapsedMillis() {
        // No elapsed time can be tracked without a first mark.
        if( lastMark == null ) { lastMark = System.currentTimeMillis(); return 0; }

        // Elapsed time is difference between this and the previous mark.
        long mark = System.currentTimeMillis();
        long elapsed = mark - lastMark;
        lastMark = mark;

        return  elapsed;
    }
}
