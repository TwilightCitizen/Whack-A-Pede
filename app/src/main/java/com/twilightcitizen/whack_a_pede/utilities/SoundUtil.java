/*
Whack-A-Pede
David A. Clark, Jr.
Integrated Product Development
MDV4910-O, C202006-01
*/

package com.twilightcitizen.whack_a_pede.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;


import androidx.preference.PreferenceManager;

import com.twilightcitizen.whack_a_pede.R;

/*
Sound Utility sets up background music and sound effects with configured track and volume settings.
*/
public class SoundUtil {
    // Sound pool for sound effects.
    private static SoundPool effectsPool;

    // Media player for background music.
    private static MediaPlayer musicPlayer;

    // IDs for sound effects.
    private static int soundIdHit;
    private static int soundIdMiss;
    private static int soundIdPowerUp;
    private static int soundIdNewRound;
    private static int soundIdGameOver;

    // Volume settings for sound effects and music.
    private static float volumeEffects;
    private static float volumeMusic;

    // Cap on number of simultaneous streams/sounds.
    private static final int MAX_STREAMS = 2;

    // Initialization flag.
    private static boolean isInitialized = false;

    // Initialize the sound utility.
    public static void initialize( Context context ) {
        setupVolumes( context );
        setupSoundEffects( context );
        setupBackgroundMusic( context );

        isInitialized = true;
    }

    // Setup the volumes of sound effects and music.
    public static void setupVolumes( Context context ) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences( context );

        volumeEffects = (float) sharedPreferences.getInt(
            context.getString( R.string.effects_volume_key ),
            context.getResources().getInteger( R.integer.volume_effects )
        ) / context.getResources().getInteger( R.integer.volume_max );

        volumeMusic = (float) sharedPreferences.getInt(
            context.getString( R.string.music_volume_key ),
            context.getResources().getInteger( R.integer.volume_music )
        ) / context.getResources().getInteger( R.integer.volume_max );
    }

    // Setup all the sound effects in a sound pool.
    private static void setupSoundEffects( Context context ) {
        // Configure audio attributes for game audio.
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();

        attrBuilder.setUsage( AudioAttributes.USAGE_GAME );

        // Initialize Sound Pool to play sound effects.
        SoundPool.Builder builder = new SoundPool.Builder();

        builder.setMaxStreams( MAX_STREAMS );
        builder.setAudioAttributes( attrBuilder.build() );

        effectsPool = builder.build();

        // Load sounds effects into pool, saving IDs.
        soundIdHit = effectsPool.load( context, R.raw.hit, 1 );
        soundIdMiss = effectsPool.load( context, R.raw.miss, 1 );
        soundIdPowerUp = effectsPool.load( context, R.raw.power_up, 1 );
        soundIdNewRound = effectsPool.load( context, R.raw.new_round, 1 );
        soundIdGameOver = effectsPool.load( context, R.raw.game_over, 1 );
    }

    // Setup all the sound effects in a sound pool.
    public static void setupBackgroundMusic( Context context ) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences( context );

        String musicTrackName = sharedPreferences.getString(
            context.getString( R.string.music_track_key ), "Mind Bender"
        ).toLowerCase().replaceAll( "[^a-zA-Z0-9]", "_" );

        int musicTrackID = context.getResources().getIdentifier(
            musicTrackName, "raw", context.getPackageName()
        );

        musicPlayer = MediaPlayer.create( context, musicTrackID );

        musicPlayer.setLooping( true );
        musicPlayer.setVolume( volumeMusic, volumeMusic );
    }

    // The following will fail gracefully, doing nothing, failing initialization.

    // Play various sound effects.
    private static void playEffect( int soundId ) {
        effectsPool.play( soundId, volumeEffects, volumeEffects, 1, 0, 1f );
    }

    public static void playHit() { if( isInitialized ) playEffect( soundIdHit ); }
    public static void playMiss() { if( isInitialized ) playEffect( soundIdMiss ); }
    public static void playPowerUp() { if( isInitialized ) playEffect( soundIdPowerUp ); }
    public static void playNewRound() { if( isInitialized ) playEffect( soundIdNewRound ); }
    public static void playGameOver() { if( isInitialized ) playEffect( soundIdGameOver ); }

    // Control background music playback.
    public static void playMusic() { if( isInitialized ) musicPlayer.start(); }

    public static void pauseMusic() {
        if( musicPlayer.isPlaying() ) if( isInitialized ) musicPlayer.pause();
    }
    public static void stopMusic() {
        if( !isInitialized ) return;

        if( musicPlayer.isPlaying() ) musicPlayer.pause();
        musicPlayer.seekTo( 0 );
    }
}