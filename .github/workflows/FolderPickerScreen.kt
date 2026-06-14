package com.localreels.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.localreels.data.VideoFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerScreen(
    onFolderSelected: (Uri, String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val recentFolders by viewModel.recentFolders.collectAsState(initial = emptyList())

    // OpenDocumentTree works on all API levels we support (API 21+).
    // The persistable permission is taken in MainActivity after this callback fires.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Extract a human-readable name from the URI path segment
            // e.g. "primary:DCIM/Camera" → "Camera"
            val name = uri.lastPathSegment
                ?.substringAfterLast(":")
                ?.substringAfterLast("/")
                ?.ifBlank { null }
                ?: "Videos"
            onFolderSelected(uri, name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My videos") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { launcher.launch(null) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Tap to select a folder",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { launcher.launch(null) }) {
                        Text("Choose folder")
                    }
                }
            }

            if (recentFolders.isNotEmpty()) {
                Text(
                    "Recent folders",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(recentFolders) { folder ->
                        RecentFolderRow(
                            folder = folder,
                            onClick = {
                                onFolderSelected(Uri.parse(folder.uri), folder.displayName)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentFolderRow(folder: VideoFolder, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(folder.displayName) },
        supportingContent = { Text("${folder.videoCount} videos") },
        leadingContent = {
            Icon(
                Icons.Outlined.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

