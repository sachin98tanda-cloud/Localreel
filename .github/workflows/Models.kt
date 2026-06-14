package com.localreels.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ── Room entity: persisted so recent folders survive app restarts ──────────
@Entity(tableName = "video_folders", indices = [Index(value = ["uri"], unique = true)])
data class VideoFolder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val displayName: String,
    val videoCount: Int = 0,
    val lastOpenedAt: Long = System.currentTimeMillis()
)

// ── In-memory only: scanned fresh from MediaStore each session ─────────────
data class VideoItem(
    val id: Long,
    val folderId: Long,
    val uri: Uri,
    val fileName: String,
    val durationMs: Long,
    val mimeType: String,
    val width: Int,
    val height: Int
)

// ── ViewModel state: held in StateFlow, never persisted ───────────────────
data class PlaybackState(
    val videos: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = true,
    val isMuted: Boolean = false,
    val seekPositionMs: Long = 0L
) {
    val currentVideo: VideoItem? get() = videos.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex < videos.lastIndex
    val hasPrev: Boolean get() = currentIndex > 0
}
