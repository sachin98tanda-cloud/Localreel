package com.localreels

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.localreels.player.VideoPlayerManager
import com.localreels.ui.FolderPickerScreen
import com.localreels.ui.PermissionRationaleScreen
import com.localreels.ui.PlayerScreen
import com.localreels.ui.theme.LocalReelsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var playerManager: VideoPlayerManager

    // Which permission we need depends on API level:
    // API 33+ → READ_MEDIA_VIDEO
    // API 23–32 → READ_EXTERNAL_STORAGE
    private val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    private val permissionGranted = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted.value = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge() was added in Activity 1.8.0 but behaves differently
        // below API 29. Calling it only on 29+ avoids visual glitches on Android 9/10.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            enableEdgeToEdge()
        }

        // Check if permission is already granted (survives process restarts)
        permissionGranted.value = checkSelfPermission(storagePermission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED

        setContent {
            LocalReelsTheme {
                if (!permissionGranted.value) {
                    // Show rationale + request button instead of crashing
                    PermissionRationaleScreen(
                        onRequest = { permissionLauncher.launch(storagePermission) }
                    )
                } else {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "picker") {
                        composable("picker") {
                            FolderPickerScreen(
                                onFolderSelected = { uri, name ->
                                    // Take persistable permission so the URI survives app restart
                                    takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                    val encoded = Uri.encode(uri.toString())
                                    navController.navigate("player/$encoded/$name")
                                }
                            )
                        }

                        composable("player/{folderUri}/{folderName}") { backStack ->
                            val folderUri = Uri.decode(
                                backStack.arguments?.getString("folderUri") ?: ""
                            ).let { Uri.parse(it) }
                            val folderName =
                                backStack.arguments?.getString("folderName") ?: "Videos"

                            val viewModel =
                                androidx.hilt.navigation.compose.hiltViewModel<
                                    com.localreels.ui.PlayerViewModel>()
                            remember(folderUri) {
                                viewModel.loadFolder(folderUri, folderName)
                                true
                            }

                            PlayerScreen(
                                playerManager = playerManager,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}
