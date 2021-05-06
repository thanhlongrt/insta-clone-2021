package com.example.instagram.room.entity

import androidx.room.Entity
import com.example.instagram.EntityMapper
import com.example.instagram.model.StoryItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/4/2021
 */

@Entity(tableName = "stories", primaryKeys = ["storyId"])
data class StoryCache(
    val uid: String = "",
    val username: String = "",
    val storyId: String = "",
    val userAvatar: String = "",
    val photoUrl: String = "",
    val videoUrl: String = "",
    val path: String = ""
)

class StoryCacheMapper @Inject constructor() : EntityMapper<StoryCache, StoryItem> {
    override fun fromEntity(entity: StoryCache): StoryItem {
        return StoryItem(
            uid = entity.uid,
            username = entity.username,
            storyId = entity.storyId,
            userAvatar = entity.userAvatar,
            photoUrl = entity.photoUrl,
            videoUrl = entity.videoUrl,
            path = entity.path,
        )
    }

    override fun fromModel(model: StoryItem): StoryCache {
        return StoryCache(
            uid = model.uid,
            username = model.username,
            storyId = model.storyId,
            userAvatar = model.userAvatar,
            photoUrl = model.photoUrl,
            videoUrl = model.videoUrl,
            path = model.path,
        )
    }

}