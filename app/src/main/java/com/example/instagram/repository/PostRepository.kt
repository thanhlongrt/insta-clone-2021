package com.example.instagram.repository

import android.content.Context
import android.net.Uri
import com.example.instagram.ImageUtils
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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

    val postDataReference =
        firebaseSource.postDataReference

    val storyDataReference =
        firebaseSource.storyDataReference

    val currentUser
        get() = firebaseSource.currentFirebaseUser
}