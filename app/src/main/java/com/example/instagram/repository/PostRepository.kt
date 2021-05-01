package com.example.instagram.repository

import android.content.Context
import android.net.Uri
import com.example.instagram.DataState
import com.example.instagram.ImageUtils
import com.example.instagram.firebase_model.Like
import com.example.instagram.firebase_model.Post
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */

class PostRepository
@Inject
constructor(
    private val firebaseSource: FirebaseSource
) {

    val profileImageStorage =
        firebaseSource.profileImageStorage

//    private val dbSource = TaskCompletionSource<Unit>()
//    private val dbTask = dbSource.task
//
//    private val storageSource = TaskCompletionSource<Unit>()
//    private val storageTask = storageSource.task

    private fun deletePostData(id: String) =
        firebaseSource.postDataReference.child(id).removeValue()
            .addOnCompleteListener {
//            dbSource.setResult(Unit)
            }

    private fun deletePhoto(path: String) =
        firebaseSource.photoStorage.child(path).delete()
            .addOnCompleteListener {
//            storageSource.setResult(Unit)
            }


    fun delete(id: String, photoPath: String): Task<Void> {
        val deletePhotoDataTask = deletePostData(id)
        val deletePhotoTask = deletePhoto(photoPath)
        return Tasks.whenAll(deletePhotoDataTask, deletePhotoTask)
    }


    suspend fun uploadPhoto(context: Context, uri: Uri, path: String): String {

        val imageSize = ImageUtils.getImageSize(context, uri)

        val data = if (imageSize > 1 * 1024 * 1024) {
            ImageUtils.compress(context, uri)
        } else
            ImageUtils.readBytes(context, uri)!!


        return firebaseSource.photoStorage
            .child(path)
            .putBytes(data)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()
    }

    @ExperimentalCoroutinesApi
    fun getAllPosts() = callbackFlow<DataState<List<Post>>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children
                    .map { it.getValue(Post::class.java)!! }
                this@callbackFlow.sendBlocking(DataState.success(posts))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(DataState.error(null, error.message))
            }
        }

        postDataReference.addValueEventListener(postListener)

        awaitClose {
            postDataReference.removeEventListener(postListener)
        }
    }

    @ExperimentalCoroutinesApi
    fun getPostsById(uid: String) = callbackFlow<DataState<List<Post>>> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children
                    .map { it.getValue(Post::class.java)!! }
                    .filter { it.uid == uid }
                this@callbackFlow.sendBlocking(DataState.success(posts))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(DataState.error(null, error.message))
            }
        }

        postDataReference.addValueEventListener(postListener)

        awaitClose {
            postDataReference.removeEventListener(postListener)
        }
    }

    @ExperimentalCoroutinesApi
    fun getAllLikes() = callbackFlow<DataState<List<Like>>> {
        val likeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likes = snapshot.children.map {
                    it.getValue(Like::class.java)
                }
                this@callbackFlow.sendBlocking(DataState.success(likes.filterNotNull()))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(DataState.error(null, error.message))
            }
        }

        likeDataReference.addListenerForSingleValueEvent(likeListener)

        awaitClose {
            likeDataReference.removeEventListener(likeListener)
        }

    }

    @ExperimentalCoroutinesApi
    fun getLikeById(uid: String, postId: String) = callbackFlow<DataState<List<Like>>> {
        val likeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likes = snapshot.children.map {
                    it.getValue(Like::class.java)
                }
                this@callbackFlow.sendBlocking(DataState.success(likes.filterNotNull().filter {
                    it.uid == uid && it.post_id == postId
                }))
            }

            override fun onCancelled(error: DatabaseError) {
                this@callbackFlow.sendBlocking(DataState.error(null, error.message))
            }
        }

        likeDataReference.addListenerForSingleValueEvent(likeListener)

        awaitClose {
            likeDataReference.removeEventListener(likeListener)
        }

    }


    val photosStorage =
        firebaseSource.photoStorage

    fun generatePostId() =
        firebaseSource.generatePostId()

    fun savePostData(id: String, data: HashMap<String, Any>) =
        firebaseSource.savePostData(id, data)

    fun generateStoryId() =
        firebaseSource.generateStoryId()

    fun saveStoryData(storyId: String, storyData: HashMap<String, Any>) =
        firebaseSource.saveStoryData(storyId, storyData)

    fun generateLikeId() =
        firebaseSource.generateLikeId()

    fun like(likeData: HashMap<String, Any>): Task<Void> {
        return firebaseSource.like(likeData)
    }

    fun unlike(likeId: String): Task<Void> {
        return firebaseSource.unlike(likeId)
    }

    val postDataReference =
        firebaseSource.postDataReference

    val storyDataReference =
        firebaseSource.storyDataReference
    val likeDataReference =
        firebaseSource.likeDataReference

    val currentUser
        get() = firebaseSource.currentFirebaseUser
}