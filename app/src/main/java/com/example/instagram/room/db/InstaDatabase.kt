package com.example.instagram.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.instagram.room.dao.PostDao
import com.example.instagram.room.dao.StoryDao
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.dao.UserDao
import com.example.instagram.room.entity.*

/**
 * Created by Thanh Long Nguyen on 5/2/2021
 */
@Database(
    entities = [StringKeyValuePair::class, UserCache::class,
        PostCache::class, StoryCache::class, UserStoryCache::class],
    version = 2
)
//@TypeConverters(MyTypeConverters::class)
abstract class InstaDatabase : RoomDatabase() {

    abstract fun stringKeyValueDao(): StringKeyValueDao

    abstract fun userDao(): UserDao

    abstract fun postDao(): PostDao

    abstract fun storyDao(): StoryDao

    companion object {
        const val DATABASE_NAME = "insta_db"
    }
}