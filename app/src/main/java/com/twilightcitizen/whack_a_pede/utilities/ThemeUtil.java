/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;

import androidx.preference.PreferenceManager;

import com.twilightcitizen.whack_a_pede.R;

/*
Theme Utility provides textures associated with themes configured in settings.
*/
public class ThemeUtil {
    // Theme simply encapsulates the textures for easy passing around and access than an array.
    public static class Theme {
        // Textures to use for particular game elements.
        private final int centipedeHeadAbove;
        private final int centipedeHeadBelow;
        private final int centipedeBodyAbove;
        private final int centipedeBodyBelow;
        private final int lawnTop;
        private final int lawnBottom;

        public Theme(
            int centipedeHeadAbove, int centipedeHeadBelow, int centipedeBodyAbove,
            int centipedeBodyBelow, int lawnTop, int lawnBottom
        ) {
            this.centipedeHeadAbove = centipedeHeadAbove;
            this.centipedeHeadBelow = centipedeHeadBelow;
            this.centipedeBodyAbove = centipedeBodyAbove;
            this.centipedeBodyBelow = centipedeBodyBelow;
            this.lawnTop = lawnTop;
            this.lawnBottom = lawnBottom;
        }

        // Expose textures for read access.
        public int getCentipedeHeadAbove() { return centipedeHeadAbove; }
        public int getCentipedeHeadBelow() { return centipedeHeadBelow; }
        public int getCentipedeBodyAbove() { return centipedeBodyAbove; }
        public int getCentipedeBodyBelow() { return centipedeBodyBelow; }
        public int getLawnTop() { return lawnTop; }
        public int getLawnBottom() { return lawnBottom; }
    }

    // Get the current theme from default shared preferences as configured in settings.
    public static Theme getConfiguredTheme( Context context ) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences( context );

        String themeName = sharedPreferences.getString(
            context.getString( R.string.color_theme_key ), "Sunny Day"
        ).toLowerCase().replaceAll( "[^a-zA-Z0-9]", "_" );

        TypedArray themeDrawables = context.getResources().obtainTypedArray(
            context.getResources().getIdentifier( themeName, "array", context.getPackageName() )
        );

        int[] themeDrawableIds = new int[ themeDrawables.length() ];

        for( int i = 0; i < themeDrawables.length(); i++ )
            themeDrawableIds[ i ] = themeDrawables.getResourceId( i, 0 );

        themeDrawables.recycle();

        return new Theme(
            themeDrawableIds[ 0 ], themeDrawableIds[ 1 ], themeDrawableIds[ 2 ],
            themeDrawableIds[ 3 ], themeDrawableIds[ 4 ], themeDrawableIds[ 5 ]
        );
    }
}
