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

import java.util.Locale;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.twilightcitizen.whack_a_pede.R;
import com.twilightcitizen.whack_a_pede.utilities.TimeUtil;

// RecyclerView Adapter for a leaderboard entry, using a Leaderboard entry ViewHolder.
public class LeaderboardAdapter extends RecyclerView.Adapter< LeaderboardAdapter.LeaderboardViewHolder > {
    // ViewHolder for a leaderboard entry.
    public static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        // Leader's score and name.
        final ImageView imageProfilePic;
        final TextView textDisplayName;
        final TextView textScore;
        final TextView textRoundsInTime;
        final TextView textPlacement;

        LeaderboardViewHolder( @NonNull View itemView ) {
            super( itemView );

            imageProfilePic = itemView.findViewById( R.id.image_profile_pic );
            textDisplayName = itemView.findViewById( R.id.text_display_name );
            textScore = itemView.findViewById( R.id.text_score );
            textRoundsInTime = itemView.findViewById( R.id.text_rounds_in_time );
            textPlacement = itemView.findViewById( R.id.text_placement );
        }
    }

    // Gap to put between leaderboard entries.
    public static class LeaderboardEntryGap extends RecyclerView.ItemDecoration {
        private int gap;

        public LeaderboardEntryGap( int gap ) { this.gap = gap; }

        @Override public void getItemOffsets(
            Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state
        ) {
            outRect.top = gap / 2;
            outRect.bottom = gap / 2;
        }
    }

    // Context for resources.
    Context context;

    // Image manager for loading images.
    ImageManager imageManager;

    // Leaderboard entries to be adapted for view.
    private final LeaderboardScoreBuffer leaderboardScores;

    // Player ID to be excluded from display.
    private final String excludePlayerID;

    public LeaderboardAdapter(
        Context context, LeaderboardScoreBuffer leaderboardScores, String excludePlayerID
    ) {
        this.context = context;
        this.leaderboardScores = leaderboardScores;
        this.imageManager = ImageManager.create( context );
        this.excludePlayerID = excludePlayerID;
    }

    // View type is determined by placement or position.
    @Override public int getItemViewType( int position ) { return position; }

    @NonNull @Override public LeaderboardViewHolder onCreateViewHolder(
        @NonNull ViewGroup parent, int viewType
    ) {
        View leaderboardItem = LayoutInflater
            .from( parent.getContext() ).inflate( R.layout.constraint_leaderboard, parent, false );

        return new LeaderboardViewHolder( leaderboardItem );
    }

    @Override public void onBindViewHolder( @NonNull LeaderboardViewHolder holder, int position ) {
        // All leaderboard items can be bound with the same view holder.
        LeaderboardScore leaderboardScore = leaderboardScores.get( position );

        holder.textDisplayName.setText( leaderboardScore.getScoreHolderDisplayName() );

        holder.textScore.setText( String.format(
            Locale.getDefault(), context.getString( R.string.top_score ), leaderboardScore.getRawScore()
        ) );

        String scoreTag = leaderboardScore.getScoreTag();
        String[] scoreTagParts = scoreTag.split( "_" );
        int roundsValue = Integer.parseInt( scoreTagParts[ 0 ] );
        long timeValue = Long.parseLong( scoreTagParts[ 1 ] );

        String rounds = context.getResources().getQuantityString(
            R.plurals.rounds, roundsValue, roundsValue
        );

        String inTime = String.format(
            Locale.getDefault(), context.getString( R.string.in_time ),
            TimeUtil.millisToMinutesAndSeconds( timeValue )
        );

        holder.textRoundsInTime.setText( String.format(
            Locale.getDefault(), context.getString( R.string.rounds_in_time ),  rounds, inTime
        ) );

        holder.textPlacement.setText( leaderboardScore.getDisplayRank() );

        imageManager.loadImage(
            holder.imageProfilePic,
            leaderboardScore.getScoreHolderHiResImageUri(),
            R.drawable.icon_guest_avatar
        );

        if( leaderboardScore.getScoreHolder().getPlayerId().equals( excludePlayerID ) ) {
            holder.itemView.setVisibility( View.GONE );
            holder.itemView.setLayoutParams( new RecyclerView.LayoutParams( 0, 0 ) );
        } else {
            holder.itemView.setVisibility( View.VISIBLE );

            holder.itemView.setLayoutParams( new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ) );
        }
    }

    @Override public int getItemCount() { return leaderboardScores.getCount(); }
}
