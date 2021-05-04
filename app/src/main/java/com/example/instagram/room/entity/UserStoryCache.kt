package com.example.instagram.room.entity

import androidx.room.Embedded
import androidx.room.Entity
import com.example.instagram.EntityMapper
import com.example.instagram.model.StoryItem
import com.example.instagram.model.UserStoryItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/4/2021
 */

@Entity(tableName = "user_stories", primaryKeys = ["uid"])
class UserStoryCache(
    val uid: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    @Embedded(prefix = "story_")
    val story: StoryCache,
) {
}

class UserStoryCacheMapper @Inject constructor(
    private val storyCacheMapper: StoryCacheMapper
): EntityMapper<UserStoryCache, UserStoryItem>{
    override fun fromEntity(entity: UserStoryCache): UserStoryItem {
        return UserStoryItem(
            uid = entity.uid,
            username = entity.username,
            avatarUrl = entity.avatarUrl,
            stories = listOf(storyCacheMapper.fromEntity(entity.story))
        )
    }

    override fun fromModel(model: UserStoryItem): UserStoryCache {
        return UserStoryCache(
            uid = model.uid,
            username =  model.username,
            avatarUrl = model.avatarUrl,
            story = storyCacheMapper.fromModel(model.stories.first())
        )
    }
}

