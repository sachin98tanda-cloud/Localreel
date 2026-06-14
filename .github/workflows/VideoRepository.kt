package com.localreels.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val folderDao: VideoFolderDao
) {
    val recentFolders = folderDao.observeAll()

    suspend fun saveFolder(folder: VideoFolder) = folderDao.upsert(folder)
    suspend fun touchFolder(uri: String) = folderDao.touch(uri)

    suspend fun loadVideosFromFolder(folderUri: Uri): List<VideoItem> =
        withContext(Dispatchers.IO) {

            // WIDTH and HEIGHT columns exist from API 16, but on some Android 12
            // ROMs the column is present yet returns 0. We query them but treat 0
            // as "unknown" and fall back to 0 gracefully.
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )

            // Extract bucket ID from the tree URI produced by OpenDocumentTree.
            // Format: content://com.android.externalstorage.documents/tree/primary%3ADCIM
            // lastPathSegment → "primary:DCIM"  →  bucketId is not directly available here.
            // We fall back to matching on BUCKET_DISPLAY_NAME when bucket ID parsing fails.
            val lastSegment = folderUri.lastPathSegment ?: ""
            val folderName = lastSegment.substringAfterLast(":").substringAfterLast("/")
            val bucketId = lastSegment.substringAfterLast(":").toLongOrNull()

            val (selection, selectionArgs) = when {
                bucketId != null ->
                    "${MediaStore.Video.Media.BUCKET_ID} = ?" to arrayOf(bucketId.toString())
                folderName.isNotBlank() ->
                    "${MediaStore.Video.Media.BUCKET_DISPLAY_NAME} = ?" to arrayOf(folderName)
                else -> null to null
            }

            // On Android 10+ (API 29+) we must use the version-aware URI
            val queryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val results = mutableListOf<VideoItem>()

            context.contentResolver.query(
                queryUri,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol      = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol    = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durCol     = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val mimeCol    = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val widthCol   = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol  = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val bucketCol  = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    results += VideoItem(
                        id        = id,
                        folderId  = cursor.getLong(bucketCol),
                        uri       = ContentUris.withAppendedId(queryUri, id),
                        fileName  = cursor.getString(nameCol) ?: "video_$id",
                        durationMs = cursor.getLong(durCol),
                        mimeType  = cursor.getString(mimeCol) ?: "video/mp4",
                        width     = if (widthCol >= 0) cursor.getInt(widthCol) else 0,
                        height    = if (heightCol >= 0) cursor.getInt(heightCol) else 0
                    )
                }
            }
            results
        }
}
