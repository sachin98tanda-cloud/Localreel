package com.localreels

import android.content.Context
import androidx.room.Room
import com.localreels.data.AppDatabase
import com.localreels.data.VideoFolderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "localreels.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideFolderDao(db: AppDatabase): VideoFolderDao = db.folderDao()
}
