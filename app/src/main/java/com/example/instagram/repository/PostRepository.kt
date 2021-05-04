package com.example.instagram.repository

import android.content.Context
import android.net.Uri
import com.example.instagram.DataState
import com.example.instagram.Status
import com.example.instagram.model.LikeItem
import com.example.instagram.model.PostItem
import com.example.instagram.network.FirebaseSource
import com.example.instagram.network.entity.Like
import com.example.instagram.network.entity.LikeNetWorkMapper
import com.example.instagram.network.entity.Post
import com.example.instagram.network.entity.PostNetworkMapper
import com.example.instagram.room.dao.PostDao
import com.example.instagram.room.entity.PostCacheMapper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import javax.inject.Inject


/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */

@ExperimentalCoroutinesApi
class PostRepository
@Inject
constructor(
    private val firebaseSource: FirebaseSource,
    private val postDao: PostDao,
    private val postNetworkMapper: PostNetworkMapper,
    private val postCacheMapper: PostCacheMapper,
    private val likeNetWorkMapper: LikeNetWorkMapper
) {

    fun deletePost(id: String, photoPath: String): Task<Void> {
        val deletePostDataTask = firebaseSource.postDataReference.child(id).removeValue()
        val deletePhotoTask = firebaseSource.photoStorage.child(photoPath).delete()
        return Tasks.whenAll(deletePostDataTask, deletePhotoTask)
    }

    suspend fun uploadPhoto(context: Context, uri: Uri, path: String): Flow<DataState<String>> =
        flow {
            val photoUrl = firebaseSource.uploadPhoto(context, uri, path)
            emit(DataState.success(photoUrl))
        }.catch { e ->
            emit(DataState.error(null, e.message))
        }.onStart {
            emit(DataState.loading())
        }

    suspend fun savePostData(
        context: Context,
        uri: Uri,
        path: String,
        postData: HashMap<String, Any>
    ) =
        uploadPhoto(context, uri, path).collect { photoUrlResult ->
            if (photoUrlResult.status == Status.SUCCESS) {
                postData["photo_url"] = photoUrlResult.data!!
                firebaseSource.savePostData(postData)
            }
        }

    fun getFeedPostsWithLikes(): Flow<DataState<List<PostItem>>> =
        getFeedPostsFromFirebase().combine(getLikes()) { postItems, likeItems ->
            val likesByPost = mutableMapOf<String, MutableList<LikeItem>>()
            likeItems.groupByTo(likesByPost) { it.postId }
            postItems.forEach { postItem ->
                var isliked = false
                likesByPost[postItem.postId]?.filter { likeItem ->
                    likeItem.uid == currentUser!!.uid && likeItem.postId == postItem.postId
                }?.size?.let {
                    isliked = it > 0
                }
                postItem.isLiked = isliked
                postItem.likes = likesByPost[postItem.postId] ?: mutableListOf()
                postItem.likeCount = likesByPost[postItem.postId]?.size?.toLong() ?: 0
            }

            val postCache = postItems.map {
                postCacheMapper.fromModel(it)
            }.takeLast(3)
            postCache.forEach {
                it.isFeedPost = true
            }
            postDao.insertFeedPosts(postCache)
            return@combine DataState.success(postItems)
        }.catch { e ->
            emit(DataState.error(null, e.message!!))
        }.onStart {
            emit(DataState.loading())
            emit(getFeedPostsFromCache())
        }

    fun getPostsByUserWithLikes(uid: String): Flow<DataState<List<PostItem>>> =
        getPostsByUserFromFirebase(uid).combine(getLikes()) { postItems, likeItems ->
            val likesByPost = mutableMapOf<String, MutableList<LikeItem>>()
            likeItems.groupByTo(likesByPost) { it.postId }
            postItems.forEach { postItem ->
                var isLiked = false
                likesByPost[postItem.postId]?.filter { likeItem ->
                    likeItem.uid == currentUser!!.uid && likeItem.postId == postItem.postId
                }?.size?.let {
                    isLiked = it > 0
                }
                postItem.isLiked = isLiked
                postItem.likes = likesByPost[postItem.postId] ?: mutableListOf()
                postItem.likeCount = likesByPost[postItem.postId]?.size?.toLong() ?: 0
            }

            val postCache = postItems.map {
                postCacheMapper.fromModel(it)
            }

            postCache.forEach {
                it.isFeedPost = false
            }
            postDao.insertPosts(postCache)

            return@combine DataState.success(postItems)
        }.catch { e ->
            emit(DataState.error(null, e.message!!))
        }.onStart {
            emit(DataState.loading())
            emit(getPostsByIdFromCache(uid))
        }

    private suspend fun getFeedPostsFromCache() =
        postDao.getFeedPosts()?.let { feedPosts ->
            DataState.success(feedPosts.map { postCacheMapper.fromEntity(it) })
        } ?: DataState.error(null, "getFeedPostsFromCache: Error")

    private fun getFeedPostsFromFirebase() = callbackFlow<List<PostItem>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkPosts = snapshot.children
                    .map { it.getValue(Post::class.java)!! }
                    .filter { it.uid != currentUser!!.uid }
                val postItems = networkPosts.map { postNetworkMapper.fromEntity(it) }
                this@callbackFlow.sendBlocking(postItems)
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(listOf())
            }
        }

        postDataReference.addListenerForSingleValueEvent(postListener)

        awaitClose {
            postDataReference.removeEventListener(postListener)
        }
    }

    private suspend fun getPostsByIdFromCache(uid: String) =
        postDao.getPostsBy(uid)?.let { posts ->
            DataState.success(posts.map { postCacheMapper.fromEntity(it) })
        } ?: DataState.error(null, "getPostsByIdFromCache: Error")

    private fun getPostsByUserFromFirebase(uid: String) = callbackFlow<List<PostItem>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkPosts = snapshot.children
                    .mapNotNull { it.getValue(Post::class.java)!! }
                    .filter { it.uid == uid }
                val postItems = networkPosts.map { postNetworkMapper.fromEntity(it) }
                this@callbackFlow.sendBlocking(postItems)
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(listOf())
            }
        }

        if (uid == currentUser!!.uid) {
            postDataReference.addValueEventListener(postListener)
        } else {
            postDataReference.addListenerForSingleValueEvent(postListener)
        }

        awaitClose {
            postDataReference.removeEventListener(postListener)
        }
    }

    private fun getLikes() = callbackFlow<List<LikeItem>> {
        val likeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkLikes = snapshot.children.mapNotNull {
                    it.getValue(Like::class.java)
                }
                val likeItems = networkLikes.map { likeNetWorkMapper.fromEntity(it) }
                this@callbackFlow.sendBlocking(likeItems)
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(listOf())
            }
        }
        likeDataReference.addListenerForSingleValueEvent(likeListener)
        awaitClose {
            likeDataReference.removeEventListener(likeListener)
        }

    }

    suspend fun like(likeData: HashMap<String, Any>) {
        firebaseSource.like(likeData)
    }

    suspend fun unlike(uid: String, postId: String) {
        getLikeById(uid, postId).collect {
            it?.let { likeItem -> firebaseSource.unlike(likeItem.likeId) }
        }
    }

    private fun getLikeById(uid: String, postId: String) = callbackFlow<LikeItem?> {
        val likeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkLike = snapshot.children.mapNotNull {
                    it.getValue(Like::class.java)
                }.first { it.uid == uid && it.post_id == postId }
                val likeItem = likeNetWorkMapper.fromEntity(networkLike)
                this@callbackFlow.sendBlocking(likeItem)
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(null)
            }
        }
        likeDataReference.addListenerForSingleValueEvent(likeListener)
        awaitClose {
            likeDataReference.removeEventListener(likeListener)
        }
    }

    private val postDataReference =
        firebaseSource.postDataReference

    private val likeDataReference =
        firebaseSource.likeDataReference

    val currentUser
        get() = firebaseSource.currentFirebaseUser
}