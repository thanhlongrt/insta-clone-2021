package com.example.instagram.ui.profile.create_new

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.firebase_model.Post
import com.example.instagram.repository.PostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.set

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
@HiltViewModel
class PostViewModel
@Inject
constructor(
    private val postRepository: PostRepository,
    application: Application
) : AndroidViewModel(application) {
    companion object {
        const val TAG = "PostViewModel"
    }

    private val _uploadResult = MutableLiveData<DataState<String>>()
    val uploadResult: LiveData<DataState<String>> = _uploadResult

    private val _savePostDataResult = MutableLiveData<DataState<Boolean>>()
    val savePostDataResult: LiveData<DataState<Boolean>> = _savePostDataResult

    private val _postLiveData = MutableLiveData<DataState<List<Post>>>()
    val postLiveData: LiveData<DataState<List<Post>>> = _postLiveData

    private val _deletePostResult = MutableLiveData<DataState<Boolean>>()
    val deletePostResult: LiveData<DataState<Boolean>> = _deletePostResult

    private val _saveStoryDataResult = MutableLiveData<DataState<Boolean>>()
    val saveStoryDataResult: LiveData<DataState<Boolean>> = _saveStoryDataResult

    fun saveStoryData(storyData: HashMap<String, Any>) {
        _saveStoryDataResult.postValue(DataState.loading())
        Log.e(TAG, "saveStoryData: Loading...")
        val id = postRepository.generateStoryId()
        storyData["story_id"] = id
        postRepository.saveStoryData(id, storyData).addOnCompleteListener {
            if (it.isSuccessful) {
                _saveStoryDataResult.postValue(DataState.success(true))
                Log.e(TAG, "saveStoryData: Success")
            } else {
                _saveStoryDataResult.postValue(DataState.error(false, it.exception?.message!!))
                Log.e(TAG, "saveStoryData: Error: ${it.exception?.message}")
            }
        }
    }

    fun deletePost(id: String, photoPath: String) {
        _deletePostResult.postValue(DataState.loading())
        Log.e(TAG, "deletePost: Loading")
        postRepository.delete(id, photoPath).addOnCompleteListener {
            _deletePostResult.postValue(DataState.success(true))
            Log.e(TAG, "deletePost: Success")
        }.addOnFailureListener {
            _deletePostResult.postValue(DataState.error(false, "deletePost: Failed"))
            Log.e(TAG, "deletePost: Failed")
        }
    }

    fun savePostData(data: HashMap<String, Any>) {
        _savePostDataResult.postValue(DataState.loading())
        Log.e(TAG, "savePostData: Loading...")
        val photoId = postRepository.generatePostId()
        data["post_id"] = photoId
        postRepository.savePostData(photoId, data).addOnCompleteListener {
            if (it.isSuccessful) {
                _savePostDataResult.postValue(DataState.success(true))
                Log.e(TAG, "savePostData: Success")
            } else {
                _savePostDataResult.postValue(DataState.error(false, it.exception?.message!!))
                Log.e(TAG, "savePostData: Error: ${it.exception?.message}")
            }
        }
    }


    fun upload(uri: Uri, storagePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(TAG, "upload: Loading...")
            _uploadResult.postValue(DataState.loading())
            try {
                val url = postRepository.uploadPhoto(getApplication(), uri, storagePath)
                Log.e(TAG, "upload: Success")
                Log.e(TAG, "upload: url: $url")
                _uploadResult.postValue(DataState.success(url))
            } catch (e: Exception) {
                Log.e(TAG, "upload: Error: ${e.message.toString()}")
                _uploadResult.postValue(DataState.error(null, e.message.toString()))
            }
        }
    }

    private var userPostsListener: ValueEventListener? = null
    fun getPosts() {
        _postLiveData.postValue(DataState.loading())
        userPostsListener = postRepository.postDataReference.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val photos = mutableListOf<Post>()
                for (item in snapshot.children) {
                    val photo = item.getValue(Post::class.java)!!
                    if (photo.uid == postRepository.currentUser?.uid) {
                        photos.add(photo)
                    }
                }
                Log.e(TAG, "onDataChange: getPosts: ${postRepository.currentUser?.uid}", )
                _postLiveData.postValue(DataState.success(photos))
            }

            override fun onCancelled(error: DatabaseError) {
                _postLiveData.postValue(DataState.error(null, error.message))
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        userPostsListener?.let { postRepository.postDataReference.removeEventListener(it) }
    }

}