package com.localreels.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localreels.data.PlaybackState
import com.localreels.data.VideoFolder
import com.localreels.data.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo: VideoRepository
) : ViewModel() {

    // ── Playback state ────────────────────────────────────────────────────
    private val _playback = MutableStateFlow(PlaybackState())
    val playback: StateFlow<PlaybackState> = _playback.asStateFlow()

    // ── Recent folders ────────────────────────────────────────────────────
    val recentFolders = repo.recentFolders

    // ── Load videos from the chosen folder URI ────────────────────────────
    fun loadFolder(folderUri: Uri, displayName: String) {
        viewModelScope.launch {
            val videos = repo.loadVideosFromFolder(folderUri)
            _playback.update { PlaybackState(videos = videos, currentIndex = 0) }

            // Persist folder for the recent list
            repo.saveFolder(
                VideoFolder(
                    uri = folderUri.toString(),
                    displayName = displayName,
                    videoCount = videos.size
                )
            )
        }
    }

    // ── Swipe navigation ──────────────────────────────────────────────────
    fun swipeNext() {
        _playback.update { state ->
            if (state.hasNext) state.copy(currentIndex = state.currentIndex + 1, seekPositionMs = 0L)
            else state
        }
    }

    fun swipePrev() {
        _playback.update { state ->
            if (state.hasPrev) state.copy(currentIndex = state.currentIndex - 1, seekPositionMs = 0L)
            else state
        }
    }

    fun onPageChanged(index: Int) {
        _playback.update { it.copy(currentIndex = index, seekPositionMs = 0L) }
    }

    // ── Playback controls ─────────────────────────────────────────────────
    fun togglePlay() {
        _playback.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun toggleMute() {
        _playback.update { it.copy(isMuted = !it.isMuted) }
    }

    fun seekTo(positionMs: Long) {
        _playback.update { it.copy(seekPositionMs = positionMs) }
    }
}
