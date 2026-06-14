package com.localreels.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.localreels.data.PlaybackState
import com.localreels.data.VideoItem
import com.localreels.player.VideoPlayerManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    playerManager: VideoPlayerManager,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.playback.collectAsState()
    val videos = state.videos

    if (videos.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { videos.size }
    )

    // Sync pager → ViewModel when user swipes
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentIndex) {
            viewModel.onPageChanged(pagerState.currentPage)
        }
    }

    // Sync ViewModel → ExoPlayer when active video or mute changes
    LaunchedEffect(state.currentIndex, state.isMuted) {
        videos.getOrNull(state.currentIndex)?.let { video ->
            playerManager.play(video, muted = state.isMuted)
        }
        videos.getOrNull(state.currentIndex + 1)?.let { playerManager.preload(it, 1) }
        videos.getOrNull(state.currentIndex - 1)?.let { playerManager.preload(it, -1) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            VideoPage(
                video = videos[page],
                isActive = page == pagerState.currentPage,
                playerManager = playerManager,
                state = state,
                onTogglePlay = viewModel::togglePlay,
                onToggleMute = viewModel::toggleMute
            )
        }

        // Back button + counter overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    // AutoMirrored variant avoids the deprecation warning on API 33+
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                "${state.currentIndex + 1} / ${videos.size}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun VideoPage(
    video: VideoItem,
    isActive: Boolean,
    playerManager: VideoPlayerManager,
    state: PlaybackState,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            // update lambda re-attaches the shared ExoPlayer instance whenever
            // this page becomes active — needed on Android 12 where the surface
            // may be detached during a fast swipe.
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = playerManager.player
                        useController = false
                    }
                },
                update = { view ->
                    if (view.player !== playerManager.player) {
                        view.player = playerManager.player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Tap to play/pause
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Outlined.PauseCircle
                              else Icons.Outlined.PlayCircle,
                contentDescription = if (state.isPlaying) "Pause" else "Play",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(48.dp)
            )
        }

        // Side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (state.isMuted) Icons.Outlined.VolumeOff
                                  else Icons.Outlined.VolumeUp,
                    contentDescription = "Toggle mute",
                    tint = Color.White
                )
            }
            IconButton(onClick = { /* share intent */ }) {
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.White)
            }
            IconButton(onClick = { /* show info */ }) {
                Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.White)
            }
        }

        // Filename + duration at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                video.fileName,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                formatDuration(video.durationMs),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

