package com.example.instagram.repository

import android.content.Context
import android.net.Uri
import com.example.instagram.DataState
import com.example.instagram.Status
import com.example.instagram.model.StoryItem
import com.example.instagram.model.UserStoryItem
import com.example.instagram.network.FirebaseSource
import com.example.instagram.network.entity.Story
import com.example.instagram.network.entity.StoryNetworkMapper
import com.example.instagram.room.dao.StoryDao
import com.example.instagram.room.entity.UserStoryCacheMapper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/3/2021
 */

@ExperimentalCoroutinesApi
class StoryRepository
@Inject
constructor(
    private val firebaseSource: FirebaseSource,
    private val storyNetworkMapper: StoryNetworkMapper,
    private val storyDao: StoryDao,
    private val userStoryCacheMapper: UserStoryCacheMapper
) {

    suspend fun getUserStories(): Flow<DataState<List<UserStoryItem>>> = flow {
        getStoriesFromFirebase().collect { storyResult ->
            val storiesByUid = storyResult.data!!.groupBy { it.uid }
            val userStoryItems = mutableListOf<UserStoryItem>()
            for (entry in storiesByUid) {
                val item = UserStoryItem(
                    uid = entry.key,
                    username = entry.value[0].username,
                    avatarUrl = entry.value[0].userAvatar,
                    stories = entry.value
                )
                userStoryItems.add(item)
            }
            emit(DataState.success(userStoryItems as List<UserStoryItem>))
            storyDao.deleteAllAndInsert(userStoryItems.map { userStoryCacheMapper.fromModel(it) })
        }
    }.catch {
        emit(DataState.error(null, it.message))
    }.onStart {
        emit(DataState.loading())
        emit(getStoriesCache())
    }

    private suspend fun getStoriesCache() =
        storyDao.getUserStoryCache()?.let { stories ->
            DataState.success(stories.map { userStoryCacheMapper.fromEntity(it) })
        } ?: DataState.error(null, "getStoriesCache: Error")

    private suspend fun getStoriesFromFirebase() = callbackFlow<DataState<List<StoryItem>>> {
        val storyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkStories = snapshot.children.map {
                    it.getValue(Story::class.java)
                }
                val storyItems = networkStories.map { storyNetworkMapper.fromEntity(it!!) }
                this@callbackFlow.sendBlocking(DataState.success(storyItems))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(DataState.error(null, error.message))
            }
        }
        firebaseSource.storyDataReference.addValueEventListener(storyListener)
        awaitClose {
            firebaseSource.storyDataReference.removeEventListener(storyListener)
        }
    }

    private suspend fun uploadPhoto(
        context: Context,
        uri: Uri,
        path: String
    ): Flow<DataState<String>> =
        flow {
            val photoUrl = firebaseSource.uploadPhoto(context, uri, path)
            emit(DataState.success(photoUrl))
        }.catch { e ->
            emit(DataState.error(null, e.message))
        }.onStart {
            emit(DataState.loading())
        }

    suspend fun saveStoryData(
        context: Context,
        uri: Uri,
        path: String,
        storyData: HashMap<String, Any>
    ) =
        uploadPhoto(context, uri, path).collect { downloadUrlResult ->
            if (downloadUrlResult.status == Status.SUCCESS) {
                storyData["photo_url"] = downloadUrlResult.data!!
                firebaseSource.saveStoryData(storyData)
            }
        }
}