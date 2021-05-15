package com.example.instagram.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.instagram.network.entity.Notification
import com.example.instagram.room.dao.*
import com.example.instagram.room.entity.*

/**
 * Created by Thanh Long Nguyen on 5/2/2021
 */
@Database(
    entities = [StringKeyValuePair::class, UserCache::class,
        PostCache::class, StoryCache::class, UserStoryCache::class, Notification::class],
    version = 1
)
abstract class InstaDatabase : RoomDatabase() {

    abstract fun stringKeyValueDao(): StringKeyValueDao

    abstract fun userDao(): UserDao

    abstract fun postDao(): PostDao

    abstract fun storyDao(): StoryDao

    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "insta_db"
    }
}