package com.example.instagram.ui.create

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.instagram.utils.Constants.KEY_POST_JSON
import com.example.instagram.utils.Constants.KEY_URI
import com.example.instagram.utils.Constants.SAVE_POST_WORK_NAME
import com.example.instagram.DataState
import com.example.instagram.utils.TypeConverters
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.Post
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.StoryRepository
import com.example.instagram.repository.UserRepository
import com.example.instagram.worker.CompressWorker
import com.example.instagram.worker.MyWorker
import com.example.instagram.worker.PostUploadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
@ExperimentalCoroutinesApi
@HiltViewModel
class CreateViewModel
@Inject
constructor(
    private val postRepository: PostRepository,
    private val storyRepository: StoryRepository,
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {
    companion object {
        const val TAG = "CreateViewModel"
    }

    private val workManager = WorkManager.getInstance(application)

    private val _savePostDataResult = MutableLiveData<DataState<Boolean>>()
    val savePostDataResult: LiveData<DataState<Boolean>> = _savePostDataResult

    private val _saveStoryDataResult = MutableLiveData<DataState<Boolean>>()
    val saveStoryDataResult: LiveData<DataState<Boolean>> = _saveStoryDataResult

    private val _currentUser = MutableLiveData<UserItem?>()
    val currentUser: LiveData<UserItem?> = _currentUser

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserFlow().collect {
                _currentUser.postValue(it)
            }
        }
    }

    fun saveStoryData(uri: Uri, storagePath: String, storyData: HashMap<String, Any>) {
        Log.e(TAG, "saveStoryData: Loading...")
        viewModelScope.launch(Dispatchers.IO) {
            storyRepository.saveStoryData(getApplication(), uri, storagePath, storyData)
        }
    }

    fun savePostData(uri: Uri, post: Post) {
        Log.e(TAG, "savePostData: Loading...")
//        viewModelScope.launch(Dispatchers.IO) {
//            postRepository.savePostData(getApplication(), uri, post)
//        }
        val inputData = Data.Builder()
            .putString(KEY_POST_JSON, TypeConverters.postToJson(post))
            .putString(KEY_URI, uri.toString())
            .build()
        val compressWorkRequest = OneTimeWorkRequestBuilder<CompressWorker>()
            .setInputData(inputData)
            .build()

        val uploadWorkRequest = OneTimeWorkRequest.from(PostUploadWorker::class.java)

        val myWork = OneTimeWorkRequest.from(MyWorker::class.java)

        workManager.beginUniqueWork(
            SAVE_POST_WORK_NAME,
            ExistingWorkPolicy.APPEND,
            compressWorkRequest
        )
            .then(uploadWorkRequest)
            .enqueue()
//        workManager.enqueue(compressWorkRequest)
    }


}