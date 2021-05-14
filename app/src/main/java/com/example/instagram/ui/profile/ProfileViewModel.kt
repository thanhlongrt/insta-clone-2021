package com.example.instagram.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.model.PostItem
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/13/2021
 */

@ExperimentalCoroutinesApi
@HiltViewModel
class ProfileViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    application: Application
) : AndroidViewModel(application) {

    companion object {
        const val TAG = "ProfileViewModel"

    }

    private val _saveUserDataResult = MutableLiveData<DataState<Boolean>>()
    val saveUserDataResult: LiveData<DataState<Boolean>> = _saveUserDataResult

    private val _uploadResult = MutableLiveData<DataState<String>>()
    val uploadResult: LiveData<DataState<String>> = _uploadResult

    private val _userPosts = MutableLiveData<DataState<List<PostItem>>>()
    val userPosts: LiveData<DataState<List<PostItem>>> get() = _userPosts

    val currentUserUid = postRepository.currentUser!!.uid

    fun logout() {
        userRepository.logout()
    }

    fun updateUserData(userData: HashMap<String, Any>) {
        viewModelScope.launch {
            userRepository.updateUserData(userData).collect {
                _saveUserDataResult.value = it
                Log.e(TAG, "updateUserData: ${it.status}")
            }
        }
    }

    fun upload(photoUri: Uri, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.uploadPhoto(getApplication(), photoUri, path).collect {
                _uploadResult.postValue(it)
                Log.e(TAG, "upload: ${it.status}")
            }
        }
    }

    fun getPostByUser(uid: String) {
        viewModelScope.launch {
            postRepository.getPostsByUser(uid).collect {
                _userPosts.value = it
//                Log.e(TAG, "getPostById: ${it.status}")
            }
        }
    }

    fun clickLike(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.onLikeClick(postId)
        }
    }

}