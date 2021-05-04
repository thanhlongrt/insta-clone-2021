package com.example.instagram

/**
 * Created by Thanh Long Nguyen on 5/3/2021
 */
interface EntityMapper<Entity, Model> {

    fun fromEntity(entity: Entity): Model

    fun fromModel(model: Model): Entity

}