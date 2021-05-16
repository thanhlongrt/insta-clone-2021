package com.example.instagram.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.instagram.Constants.KEY_COMPRESSED_FILE_URI
import com.example.instagram.Constants.KEY_POST_JSON
import com.example.instagram.TypeConverters
import com.example.instagram.network.firebase.FirebaseService
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.ktx.storageMetadata
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/11/2021
 */

@ExperimentalCoroutinesApi
@HiltWorker
class PostUploadWorker
@AssistedInject
constructor(
    @Assisted
    context: Context,
    @Assisted
    params: WorkerParameters,
    val firebaseService: FirebaseService
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PostUploadWorker"
    }

    override suspend fun doWork(): Result {
        Log.e(TAG, "doWork: Start uploading")
        try {
            val resourceUri = inputData.getString(KEY_COMPRESSED_FILE_URI)
            val postJson = inputData.getString(KEY_POST_JSON)
            val post = TypeConverters.jsonToPost(postJson!!)


            withContext(Dispatchers.IO) {
                if (post.is_video) {
                    val metadata = storageMetadata {
                        contentType = "video/*"
                    }
                    post.video_url =
                        firebaseService.storageReference.child(post.path)
                            .putFile(Uri.parse(resourceUri), metadata).await()
                            .storage.downloadUrl.await().toString()
                } else {
                    post.photo_url =
                        firebaseService.storageReference.child(post.path)
                            .putFile(Uri.parse(resourceUri)).await()
                            .storage.downloadUrl.await().toString()
                }
                firebaseService.savePostData(post.toMap())
                firebaseService.userDataReference(post.uid).child("post_count").setValue(ServerValue.increment(1))
            }
            Log.e(TAG, "doWork: Success")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Fail: ${e.message}")
            return Result.failure()
        }

    }
}