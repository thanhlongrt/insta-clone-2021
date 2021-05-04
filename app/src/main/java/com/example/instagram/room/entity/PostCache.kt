package com.example.instagram.room.entity

import androidx.room.Entity
import com.example.instagram.EntityMapper
import com.example.instagram.model.PostItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/3/2021
 */
@Entity(tableName = "posts", primaryKeys = ["postId"])
class PostCache(
    val postId: String = "",
    val uid: String = "",
    val avatarUrl: String = "",
    val userName: String = "",
    val photoUrl: String = "",
    val date: Long = 0,
    val caption: String = "",
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val path: String,
    var isLiked: Boolean = false,
    var isFeedPost: Boolean = false
) {
}

class PostCacheMapper @Inject constructor(): EntityMapper<PostCache, PostItem> {
    override fun fromEntity(entity: PostCache): PostItem {
        return PostItem(
            postId = entity.postId,
            uid = entity.uid,
            avatarUrl = entity.avatarUrl,
            userName = entity.userName,
            photoUrl = entity.photoUrl,
            date = entity.date,
            caption = entity.caption,
            likeCount = entity.likeCount,
            commentCount = entity.commentCount,
            likes = mutableListOf(),
            comments = mutableListOf(),
            path = entity.path,
            isLiked = entity.isLiked
        )
    }

    override fun fromModel(model: PostItem): PostCache {
        return PostCache(
            postId = model.postId,
            uid = model.uid,
            avatarUrl = model.avatarUrl,
            userName = model.userName,
            photoUrl = model.photoUrl,
            date = model.date,
            caption = model.caption,
            likeCount = model.likes.size.toLong(),
            commentCount = model.comments.size.toLong(),
            path = model.path,
            isLiked = model.isLiked
        )
    }

}