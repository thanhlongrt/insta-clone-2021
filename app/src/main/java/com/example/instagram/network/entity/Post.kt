package com.example.instagram.network.entity

import com.example.instagram.EntityMapper
import com.example.instagram.model.PostItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
data class Post(
    val uid: String = "",
    val post_id: String = "",
    val avatar_url: String = "",
    val user_name: String = "",
    val photo_url: String = "",
    val date_created: Long = 0,
    val caption: String = "",
    val path: String = "",
    var like_count: Long = 0,
    val comment_count: Long = 0,
    var likes: MutableMap<String, Boolean> = HashMap(),
) {
}

class PostNetworkMapper @Inject constructor() : EntityMapper<Post, PostItem> {

    override fun fromEntity(entity: Post): PostItem {
        return PostItem(
            uid = entity.uid,
            postId = entity.post_id,
            avatarUrl = entity.avatar_url,
            userName = entity.user_name,
            photoUrl = entity.photo_url,
            date = entity.date_created,
            caption = entity.caption,
            path = entity.path,
            likeCount = entity.like_count,
            commentCount = entity.comment_count,
            likes = entity.likes.keys.toMutableList(),
        )
    }

    override fun fromModel(model: PostItem): Post {
        return Post(
            uid = model.uid,
            post_id = model.postId,
            avatar_url = model.avatarUrl,
            user_name = model.userName,
            photo_url = model.photoUrl,
            date_created = model.date,
            caption = model.caption,
            path = model.path,
            like_count = model.likeCount,
            comment_count = model.commentCount,
        )
    }
}