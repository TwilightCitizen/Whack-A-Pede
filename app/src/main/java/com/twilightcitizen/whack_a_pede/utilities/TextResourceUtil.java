/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
TextResourceUtil provides utility methods for the manipulation of raw text resources.  It aids
greatly in OpenGL programming, allowing OpenGL shader programs to be maintained as raw GLSL files
for dynamic reading and loading into OpenGL rather than hardcoding them as Strings throughout the
codebase.  This borrows from OpenGL ES 2.0 for Android by Kevin Brothaler.
*/
public class TextResourceUtil {
    // Read a text resource line-by-line, building it up into a String value to return.
    public static String readTextResource( Context context, int resourceId ) {
        // String builder in which to append the lines of the text resource to be read.
        StringBuilder body = new StringBuilder();

        try {
            // Open the text resource for buffered stream reading.
            InputStream inputStream = context.getResources().openRawResource( resourceId );
            InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
            BufferedReader bufferedReader = new BufferedReader( inputStreamReader );

            // Get the first line, if any.
            String nextLine;

            // If there is a line, append it to the String builder and repeat.
            while( ( nextLine = bufferedReader.readLine() ) != null ) {
                body.append( nextLine );
                body.append( "\n" );
            }
        } catch( IOException e ) {
            throw new RuntimeException( "Could not read resource: " + resourceId, e );
        } catch( Resources.NotFoundException e ) {
            throw new RuntimeException( "Resource not found: " + resourceId, e );
        }

        // Build the String and return it.
        return body.toString();
    }
}