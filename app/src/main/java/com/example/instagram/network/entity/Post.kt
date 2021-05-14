package com.example.instagram.network.entity

import com.example.instagram.EntityMapper
import com.example.instagram.model.PostItem
import com.google.firebase.database.PropertyName
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
data class Post(
    val uid: String = "",
    val post_id: String = "",
    val avatar_url: String = "",
    val user_name: String = "",
    var photo_url: String = "",
    var video_url: String = "",
    val date_created: Long = 0,
    val caption: String = "",
    val path: String = "",
    var like_count: Long = 0,
    val comment_count: Long = 0,
    var is_video: Boolean = false,
    var likes: MutableMap<String, Boolean> = HashMap(),
) {

    fun getIs_video(): Boolean{
        return is_video
    }

    fun toMap(): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data["uid"] = uid
        data["post_id"] = post_id
        data["avatar_url"] = avatar_url
        data["user_name"] = user_name
        data["photo_url"] = photo_url
        data["video_url"] = video_url
        data["date_created"] = date_created
        data["caption"] = caption
        data["path"] = path
        data["like_count"] = like_count
        data["comment_count"] = comment_count
        data["is_video"] = is_video
        data["likes"] = likes
        return data
    }
}

class PostNetworkMapper @Inject constructor() : EntityMapper<Post, PostItem> {

    override fun fromEntity(entity: Post): PostItem {
        return PostItem(
            uid = entity.uid,
            postId = entity.post_id,
            avatarUrl = entity.avatar_url,
            userName = entity.user_name,
            photoUrl = entity.photo_url,
            videoUrl = entity.video_url,
            date = entity.date_created,
            caption = entity.caption,
            path = entity.path,
            likeCount = entity.like_count,
            commentCount = entity.comment_count,
            isVideo = entity.is_video,
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
            video_url = model.videoUrl,
            date_created = model.date,
            caption = model.caption,
            path = model.path,
            like_count = model.likeCount,
            is_video = model.isVideo,
            comment_count = model.commentCount,
        )
    }
}