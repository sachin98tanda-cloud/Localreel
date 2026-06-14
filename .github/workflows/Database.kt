package com.localreels.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoFolderDao {
    @Query("SELECT * FROM video_folders ORDER BY lastOpenedAt DESC")
    fun observeAll(): Flow<List<VideoFolder>>

    @Upsert
    suspend fun upsert(folder: VideoFolder)

    @Query("UPDATE video_folders SET lastOpenedAt = :time WHERE uri = :uri")
    suspend fun touch(uri: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM video_folders WHERE uri = :uri")
    suspend fun delete(uri: String)
}

@Database(entities = [VideoFolder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): VideoFolderDao
}
