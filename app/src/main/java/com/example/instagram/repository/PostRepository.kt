package com.example.instagram.repository

import android.content.Context
import android.net.Uri
import com.example.instagram.DataState
import com.example.instagram.network.entity.PostNetworkMapper
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.room.dao.PostDao
import com.example.instagram.room.entity.PostCacheMapper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject


/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */

@ExperimentalCoroutinesApi
class PostRepository
@Inject
constructor(
    private val firebaseService: FirebaseService,
    private val postDao: PostDao,
    private val postNetworkMapper: PostNetworkMapper,
    private val postCacheMapper: PostCacheMapper,
) {
    companion object {
        private const val TAG = "PostRepository"
    }

    val currentUser
        get() = firebaseService.currentFirebaseUser

    fun getPostById(postId: String) =
        firebaseService.getPostById(postId)
            .map { post ->
                val postItem = postNetworkMapper.fromEntity(post!!)
                DataState.success(postItem)
            }.catch { emit(DataState.error(null, it.message)) }
            .onStart { emit(DataState.loading()) }

    fun deletePost(postId: String, mediaPath: String): Task<Void> {
        val deletePostDataTask = firebaseService.postDataReference.child(postId).removeValue()
        val decreasePostCountTask =
            firebaseService.userDataReference(currentUser!!.uid).child("post_count")
                .setValue(ServerValue.increment(-1))
        val deletePhotoTask =
            firebaseService.photoStorage.child(currentUser!!.uid).child(mediaPath).delete()
        return Tasks.whenAll(deletePostDataTask, decreasePostCountTask, deletePhotoTask)
    }

    suspend fun uploadImage(context: Context, uri: Uri, path: String): Flow<DataState<String>> =
        flow {
            val photoUrl = firebaseService.uploadPhoto(context, uri, path)
            emit(DataState.success(photoUrl))
        }.onStart {
            emit(DataState.loading())
        }.catch { e ->
            emit(DataState.error(null, e.message))
        }

    private suspend fun getFeedPostsFromCache() =
        postDao.getFeedPosts()?.let { feedPosts ->
            DataState.success(feedPosts.reversed().map { postCacheMapper.fromEntity(it) })
        } ?: DataState.error(null, "getFeedPostsFromCache: Error")

    fun fetchFeedPosts() =
        firebaseService.getFeedPostsFromFirebase().map { result ->
            val postItems = result!!.map { postNetworkMapper.fromEntity(it) }
            postItems.forEach {
                it.isLiked = it.likes.contains(currentUser!!.uid)
            }
            val feedPostCache = postItems.map { postCacheMapper.fromModel(it) }
            feedPostCache.forEach { it.isFeedPost = true }
            postDao.insertFeedPosts(feedPostCache)
            return@map DataState.success(postItems)
        }.onStart {
            emit(DataState.loading())
            emit(getFeedPostsFromCache())
        }.catch { emit(DataState.error(null, it.message)) }


    private suspend fun getPostsByUserFromCache(uid: String) =
        postDao.getPostsBy(uid)?.let { posts ->
            DataState.success(posts.map { postCacheMapper.fromEntity(it) })
        } ?: DataState.error(null, "getPostsByIdFromCache: Error")

    fun getPostsByUserFromFirebase(uid: String) =
        firebaseService.getPostsByUserFromFirebase(uid).map { result ->
            val postItems = result!!.map { postNetworkMapper.fromEntity(it) }
            postItems.forEach {
                it.isLiked = it.likes.contains(currentUser?.uid)
            }
            if (uid == currentUser?.uid) {
                val userPostCache = postItems.map { postCacheMapper.fromModel(it) }
                postDao.insertPosts(userPostCache)
            }

            return@map DataState.success(postItems)
        }.onStart {
            emit(DataState.loading())
            emit(getPostsByUserFromCache(uid))
        }.catch { emit(DataState.error(null, it.message!!)) }

    fun likePost(postId: String) {
        firebaseService.likePost(postId)
    }
}