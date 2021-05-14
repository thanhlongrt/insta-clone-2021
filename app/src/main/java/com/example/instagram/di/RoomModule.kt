package com.example.instagram.di

import android.content.Context
import androidx.room.Room
import com.example.instagram.room.dao.*
import com.example.instagram.room.db.InstaDatabase
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Created by Thanh Long Nguyen on 5/2/2021
 */
@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Singleton
    @Provides
    fun provideInstaDatabase(
        @ApplicationContext context: Context,
    ): InstaDatabase {
        return Room.databaseBuilder(
            context,
            InstaDatabase::class.java,
            InstaDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideStringKeyValueDao(
        database: InstaDatabase
    ): StringKeyValueDao {
        return database.stringKeyValueDao()
    }

    @Singleton
    @Provides
    fun provideUserDao(database: InstaDatabase): UserDao {
        return database.userDao()
    }

    @Singleton
    @Provides
    fun providePostDao(database: InstaDatabase): PostDao {
        return database.postDao()
    }

    @Singleton
    @Provides
    fun provideStoryDao(database: InstaDatabase): StoryDao {
        return database.storyDao()
    }

    @Singleton
    @Provides
    fun provideNotificationDao(database: InstaDatabase): NotificationDao {
        return database.notificationDao()
    }

//    @Singleton
//    @Provides
//    fun provideMyTypeConverter(
//        gson: Gson
//    ): MyTypeConverters {
//        return MyTypeConverters()
//    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }
}