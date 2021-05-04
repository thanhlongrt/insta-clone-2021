package com.example.instagram.room.dao

import androidx.room.*
import com.example.instagram.room.entity.UserStoryCache

/**
 * Created by Thanh Long Nguyen on 5/4/2021
 */

@Dao
interface StoryDao {

    @Query("SELECT * FROM user_stories")
    suspend fun getUserStoryCache(): List<UserStoryCache>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(storyCache: UserStoryCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<UserStoryCache>)

    @Query("DELETE FROM  stories")
    suspend fun deleteAll()

    @Transaction
    suspend fun deleteAllAndInsert(stories: List<UserStoryCache>) {
        deleteAll()
        insertStories(stories)
    }
}