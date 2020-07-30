/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.utilities.ThemeUtil;

/*
Theme preview preference is a custom settings preference to show an image preview of the user
configured theme.
*/
public class ThemePreviewPreference extends Preference {
    private ImageView imageLawnBottom;
    private ImageView imageLawnTop;
    private ImageView imageHeadBelow;
    private ImageView imageBodyBelow;
    private ImageView imageHeadAbove;
    private ImageView imageBodyAbove;

    public ThemePreviewPreference( Context context, AttributeSet attrs ) { super( context, attrs ); }

    @Override public void onBindViewHolder( PreferenceViewHolder holder ) {
        super.onBindViewHolder( holder );

        imageLawnBottom = (ImageView) holder.findViewById( R.id.image_lawn_bottom );
        imageLawnTop = (ImageView) holder.findViewById( R.id.image_lawn_top );
        imageHeadBelow = (ImageView) holder.findViewById( R.id.image_head_below );
        imageBodyBelow = (ImageView) holder.findViewById( R.id.image_body_below );
        imageHeadAbove = (ImageView) holder.findViewById( R.id.image_head_above );
        imageBodyAbove = (ImageView) holder.findViewById( R.id.image_body_above );

        setThemeToPreview( ThemeUtil.getConfiguredTheme( getContext() ) );
    }

    public void setThemeToPreview( ThemeUtil.Theme theme ) {
        imageLawnBottom.setImageResource( theme.getLawnBottom() );
        imageLawnTop.setImageResource( theme.getLawnTop() );
        imageHeadBelow.setImageResource( theme.getCentipedeHeadBelow() );
        imageBodyBelow.setImageResource( theme.getCentipedeBodyBelow() );
        imageHeadAbove.setImageResource( theme.getCentipedeHeadAbove() );
        imageBodyAbove.setImageResource( theme.getCentipedeBodyAbove() );
    }
}
