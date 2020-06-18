package com.twilightcitizen.whack_a_pede.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.*;

/*
TexureUtil provides a utility method for loading a texture into OpenGl from DPI-independent bitmap
resources with appropriate scaling, min- and magnification, and mipmapping. This borrows from OpenGL
ES 2.0 for Android by Kevin Brothaler.
*/
public class TextureUtil {
    // Tag for filtering any debug message logged.
    private static final String TAG = "TextureUtil";

    // Load a texture into OpenGL from a DPI-independent bitmap resource.
    public static int LoadTexture( Context context, int resourceId ) {
        // Hold the status of generating a new texture on the graphics hardware.
        final int[] textures = new int[ 1 ];

        // Generating a new texture on the graphics hardware.
        glGenTextures( 1, textures, 0 );

        // Log texture generation failure when debugging.
        if( textures[ 0 ] == 0 ) {
            if( LoggerUtil.DEBUGGING ) Log.w( TAG, "Could not generate new texture." );

            return 0;
        }

        // Configure bitmap creation options to avoid scaling.
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Scaling can make bitmap dimensions not a power of 2 which can cause issues.
        options.inScaled = false;
        options.inPremultiplied = false;

        // Decode the bitmap resource
        final Bitmap bitmap = BitmapFactory.decodeResource(
            context.getResources(), resourceId, options
        );

        // Log bitmap decoding issues when debugging.
        if( bitmap == null ) {
            if( LoggerUtil.DEBUGGING )
                Log.w( TAG, "Resource ID " + resourceId + " could not be decoded." );

            // Also delete the texture from the graphics hardware since it will have no bitmap.
            glDeleteTextures( 1, textures, 0 );

            return 0;
        }

        // Configure the texture as a 2D.
        glBindTexture( GL_TEXTURE_2D, textures[ 0 ] );
        // Configure the texture with quality min- and magnification sampling for mipmaps.
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        // Bind the decoded bitmap to the texture.
        texImage2D( GL_TEXTURE_2D, 0, bitmap, 0 );

        // Free up the bitmap as it is no longer needed.
        bitmap.recycle();

        // Generate the mipmap for the texture.
        glGenerateMipmap( GL_TEXTURE_2D );

        // Make sure any other texture commands do not affect this texture.
        glBindTexture( GL_TEXTURE_2D, 0 );

        return textures[ 0 ];
    }
}
