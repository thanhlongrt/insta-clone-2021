package com.example.instagram.room

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.instagram.model.LikeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/2/2021
 */
@ProvidedTypeConverter
class MyTypeConverters {

    @TypeConverter
    fun likesToString(likes: List<LikeItem>): String {
        return Gson().toJson(likes)
    }

    @TypeConverter
    fun stringToLikes(data: String?): List<LikeItem> {
        if (data == null) {
            return listOf()
        } else {
            val listType = object : TypeToken<List<LikeItem>>() {}.type
            return Gson().fromJson(data, listType)
        }
    }

}