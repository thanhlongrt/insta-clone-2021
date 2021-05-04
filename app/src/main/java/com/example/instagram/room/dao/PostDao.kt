package com.example.instagram.room.dao

import androidx.room.*
import com.example.instagram.room.entity.PostCache

/**
 * Created by Thanh Long Nguyen on 5/3/2021
 */

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostCache>)

    @Query("SELECT * FROM posts WHERE uid = :uid")
    suspend fun getPostsBy(uid: String): List<PostCache>?

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deletePostById(postId: String)

    @Query("SELECT * FROM posts WHERE isFeedPost = 1 ORDER BY date ASC LIMIT 3")
    suspend fun getFeedPosts(): List<PostCache>?

    @Query("DELETE FROM  posts WHERE isFeedPost = 1")
    suspend fun deleteFeedPosts()

    @Transaction
    suspend fun insertFeedPosts(posts: List<PostCache>) {
        deleteFeedPosts()
        insertPosts(posts)
    }
}