package com.twilightcitizen.whack_a_pede.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.*;

public class TextureUtil {
    private static final String TAG = "TextureUtil";

    public static int LoadTexture( Context context, int resourceId ) {
        final int[] textures = new int[ 1 ];

        glGenTextures( 1, textures, 0 );

        if( textures[ 0 ] == 0 ) {
            if( LoggerUtil.DEBUGGING ) Log.w( TAG, "Could not generate new texture." );

            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(
            context.getResources(), resourceId, options
        );

        if( bitmap == null ) {
            if( LoggerUtil.DEBUGGING )
                Log.w( TAG, "Resource ID " + resourceId + " could not be decoded." );

            glDeleteTextures( 1, textures, 0 );

            return 0;
        }

        glBindTexture( GL_TEXTURE_2D, textures[ 0 ] );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        texImage2D( GL_TEXTURE_2D, 0, bitmap, 0 );

        bitmap.recycle();

        glGenerateMipmap( GL_TEXTURE_2D );
        glBindTexture( GL_TEXTURE_2D, 0 );

        return textures[ 0 ];
    }
}
