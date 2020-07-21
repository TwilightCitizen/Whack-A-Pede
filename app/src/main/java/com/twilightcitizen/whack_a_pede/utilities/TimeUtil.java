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
}
