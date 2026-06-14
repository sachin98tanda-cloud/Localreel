package com.localreels.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.localreels.data.VideoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // One ExoPlayer instance reused across videos (no recreation overhead)
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .build()
        .apply {
            repeatMode = Player.REPEAT_MODE_ONE   // loop current video
            playWhenReady = true
        }

    // Preload slots: current + next + prev
    private val preloadedItems = mutableMapOf<Int, MediaItem>()

    fun play(video: VideoItem, seekToMs: Long = 0L, muted: Boolean = false) {
        val item = MediaItem.fromUri(video.uri)
        player.apply {
            setMediaItem(item)
            prepare()
            seekTo(seekToMs)
            volume = if (muted) 0f else 1f
            play()
        }
    }

    fun preload(video: VideoItem, slot: Int) {
        preloadedItems[slot] = MediaItem.fromUri(video.uri)
    }

    fun toggleMute(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
    }

    fun togglePlay(playing: Boolean) {
        if (playing) player.play() else player.pause()
    }

    fun release() {
        player.release()
    }
}
