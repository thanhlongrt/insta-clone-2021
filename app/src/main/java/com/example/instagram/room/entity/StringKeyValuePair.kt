package com.example.instagram.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Thanh Long Nguyen on 5/2/2021
 */
@Entity(tableName = "value_pairs")
class StringKeyValuePair(
    @PrimaryKey
    val key: String,
    val value: String
) {
}