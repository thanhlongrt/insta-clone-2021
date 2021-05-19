package com.example.instagram.repository

import android.content.Context
import android.net.Uri
import com.example.instagram.DataState
import com.example.instagram.model.PostItem
import com.example.instagram.network.entity.Post
import com.example.instagram.network.entity.PostNetworkMapper
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.room.dao.PostDao
import com.example.instagram.room.entity.PostCacheMapper
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
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
    private val firebaseService: FirebaseService,
    private val postDao: PostDao,
    private val postNetworkMapper: PostNetworkMapper,
    private val postCacheMapper: PostCacheMapper,
) {
    companion object {
        private const val TAG = "PostRepository"
    }

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
        }.catch { e ->
            emit(DataState.error(null, e.message))
        }.onStart {
            emit(DataState.loading())
        }

    fun getFeedPosts(): Flow<DataState<List<PostItem>>> = flow {
        getFeedPostsFromFirebase().collect { feedPost ->
            emit(DataState.success(feedPost))
            val feedPostCache = feedPost.map { postCacheMapper.fromModel(it) }
            feedPostCache.forEach { it.isFeedPost = true }
            postDao.insertFeedPosts(feedPostCache)
        }
    }.catch { e ->
        emit(DataState.error(null, e.message!!))
    }.onStart {
        emit(DataState.loading())
        emit(getFeedPostsFromCache())
    }

    fun getPostsByUser(uid: String): Flow<DataState<List<PostItem>>> = flow {
        getPostsByUserFromFirebase(uid).collect { userPosts ->
            emit(DataState.success(userPosts))

            val userPostCache = userPosts.map { postCacheMapper.fromModel(it) }
            postDao.insertPosts(userPostCache)
        }
    }.catch { e ->
        emit(DataState.error(null, e.message!!))
    }.onStart {
        emit(DataState.loading())
        emit(getPostsByUserFromCache(uid))
    }

    private suspend fun getFeedPostsFromCache() =
        postDao.getFeedPosts()?.let { feedPosts ->
            DataState.success(feedPosts.reversed().map { postCacheMapper.fromEntity(it) })
        } ?: DataState.error(null, "getFeedPostsFromCache: Error")

    private fun getFeedPostsFromFirebase() = callbackFlow<List<PostItem>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkPosts = snapshot.children
                    .map { it.getValue(Post::class.java)!! }
                    .filter { it.uid != currentUser!!.uid }

                val postItems = networkPosts.map { postNetworkMapper.fromEntity(it) }
                postItems.forEach {
                    it.isLiked = it.likes.contains(currentUser!!.uid)
                }
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

    private suspend fun getPostsByUserFromCache(uid: String) =
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
                postItems.forEach {
                    it.isLiked = it.likes.contains(currentUser?.uid)
                }
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

    fun likePost(postId: String) {
        postDataReference.child(postId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val post = currentData.getValue(Post::class.java)
                    ?: return Transaction.success(currentData)
                val uid = currentUser!!.uid
                if (post.likes.contains(uid)) {
                    post.like_count = post.like_count - 1
                    post.likes.remove(uid)
                } else {
                    post.like_count = post.like_count + 1
                    post.likes[uid] = true
                }
                currentData.value = post
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }
        })
    }

    private val postDataReference =
        firebaseService.postDataReference

    val currentUser
        get() = firebaseService.currentFirebaseUser
}