package com.example.instagram.ui.create

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    application: Application
) : AndroidViewModel(application) {
    companion object {
        const val TAG = "PostViewModel"
    }

    private val _savePostDataResult = MutableLiveData<DataState<Boolean>>()
    val savePostDataResult: LiveData<DataState<Boolean>> = _savePostDataResult

    private val _saveStoryDataResult = MutableLiveData<DataState<Boolean>>()
    val saveStoryDataResult: LiveData<DataState<Boolean>> = _saveStoryDataResult

    fun saveStoryData(uri: Uri, storagePath: String, storyData: HashMap<String, Any>) {
        Log.e(TAG, "saveStoryData: Loading...")
        viewModelScope.launch(Dispatchers.IO) {
            storyRepository.saveStoryData(getApplication(), uri, storagePath, storyData)
        }
    }

    fun deletePost(id: String, photoPath: String) {
        postRepository.deletePost(id, photoPath)
    }

    fun savePostData(uri: Uri, storagePath: String, data: HashMap<String, Any>) {
        Log.e(TAG, "savePostData: Loading...")
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.savePostData(getApplication(), uri, storagePath, data)
        }
    }

}