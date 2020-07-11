package com.twilightcitizen.whack_a_pede.recyclerViews;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.twilightcitizen.whack_a_pede.R;

import java.util.ArrayList;

// RecyclerView Adapter for an achievement entry, using an Achievement entry ViewHolder.
public class AchievementAdapter extends RecyclerView.Adapter< AchievementAdapter.AchievementViewHolder > {
    // ViewHolder for an achievement entry.
    public static class AchievementViewHolder extends RecyclerView.ViewHolder {
        // Achievement avatar.
        final ImageView imageAchievementAvatar;

        AchievementViewHolder( @NonNull View itemView ) {
            super( itemView );

            imageAchievementAvatar = itemView.findViewById( R.id.image_achievement_avatar );
        }
    }

    // Gap to put between achievement entries.
    public static class AchievementEntryGap extends RecyclerView.ItemDecoration {
        private final int gap;

        public AchievementEntryGap( int gap ) { this.gap = gap; }

        @Override public void getItemOffsets(
            Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state
        ) {
            outRect.left = gap / 2;
            outRect.right = gap / 2;
        }
    }

    // Context for resources.
    private final Context context;

    // Image manager for loading images.
    private final ImageManager imageManager;

    // Achievements to be adapted for view.
    private final AchievementBuffer achievements;
    private final ArrayList< Integer > unlockedAchievements = new ArrayList<>();

    public AchievementAdapter(
        Context context, AchievementBuffer achievements
    ) {
        this.context = context;
        this.achievements = achievements;
        this.imageManager = ImageManager.create( context );

        indexUnlockedAchievements();
    }

    // Index all the achievements that are unlocked.
    private void indexUnlockedAchievements() {
        for( int i = 0; i < achievements.getCount(); i++ ) if(
            achievements.get( i ).getState() == Achievement.STATE_UNLOCKED
        ) {
            unlockedAchievements.add( i );
        }
    }

    @NonNull @Override public AchievementViewHolder onCreateViewHolder(
        @NonNull ViewGroup parent, int viewType
    ) {
        View achievementItem = LayoutInflater
            .from( parent.getContext() ).inflate( R.layout.contstraint_achievement, parent, false );

        return new AchievementViewHolder( achievementItem );
    }

    @Override public void onBindViewHolder( @NonNull AchievementViewHolder holder, int position ) {
        // All achievements can be bound with the same view holder.
        Achievement achievement = achievements.get( unlockedAchievements.get( position ) );

        imageManager.loadImage(
            holder.imageAchievementAvatar,
            achievement.getUnlockedImageUri(),
            R.drawable.icon_leaderboard
        );
    }

    // Expose the count of unlocked achievements.
    @Override public int getItemCount() {
        return unlockedAchievements.size();
    }
}
