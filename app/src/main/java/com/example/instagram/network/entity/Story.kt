package com.example.instagram.network.entity

import com.example.instagram.EntityMapper
import com.example.instagram.model.StoryItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/19/2021
 */
class Story(
    val uid: String = "",
    val username: String = "",
    val story_id: String = "",
    val avatar_url: String = "",
    val photo_url: String = "",
    val video_url: String = "",
    val path: String = ""
) {
}

class StoryNetworkMapper @Inject constructor() : EntityMapper<Story, StoryItem> {
    override fun fromEntity(entity: Story): StoryItem {
        return StoryItem(
            uid = entity.uid,
            storyId = entity.story_id,
            username = entity.username,
            userAvatar = entity.avatar_url,
            photoUrl = entity.photo_url,
            videoUrl = entity.video_url,
            path = entity.path,
        )
    }

    override fun fromModel(model: StoryItem): Story {
        return Story(
            uid = model.uid,
            story_id = model.storyId,
            username = model.username,
            avatar_url = model.userAvatar,
            photo_url = model.photoUrl,
            video_url = model.videoUrl,
            path = model.path,
        )
    }

}