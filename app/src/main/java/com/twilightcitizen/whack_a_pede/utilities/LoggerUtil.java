/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

/*
LoggerUtil provides a single boolean flag to indicate whether or not logging is turned on.  This is
used throughout the application to log important debugging messages when it is on, but eschew logging
when it is turned off.  This is particularly important when logging messages related to OpenGL
programming.  OpenGL programs can fail silently, crash, or produce unexpected screen outputs
without providing any useful indication of what is wrong.  OpenGL internally logs these errors,
however, and these can be forwarded to the debugging logger for examination.  Logging can also
slow down important rendering routines, too, though, so it should be turned off once debugging
is complete and the application is working and released.  The standard debug logger is used for
the actual logging of any messages, and it is called at the location where messages should be
logged rather than from within LoggerUtil via static methods that check this instead.  This
strategy helps to boost performance during debugging somewhat by voiding the needless creation
of an additional stack frame just to consolidate all logging functionality here.  This strategy
borrows from that shown in OpenGL ES 2.0 for Android by Kevin Brothaler.
*/
public class LoggerUtil {
    public static final boolean ON = true;
}
