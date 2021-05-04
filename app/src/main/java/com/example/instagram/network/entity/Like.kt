package com.example.instagram.network.entity

import com.example.instagram.EntityMapper
import com.example.instagram.model.LikeItem
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class Like(
    val uid: String = "",
    val like_id: String = "",
    val post_id: String = "",
)

class LikeNetWorkMapper @Inject constructor() : EntityMapper<Like, LikeItem> {
    override fun fromEntity(entity: Like): LikeItem {
        return LikeItem(
            uid = entity.uid,
            likeId = entity.like_id,
            postId = entity.post_id,
        )
    }

    override fun fromModel(model: LikeItem): Like {
        return Like(
            uid = model.uid,
            like_id = model.likeId,
            post_id = model.postId
        )
    }

}