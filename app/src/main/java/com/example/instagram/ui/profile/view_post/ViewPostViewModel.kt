package com.example.instagram.ui.profile.view_post

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.model.PostItem
import com.example.instagram.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/14/2021
 */
@ExperimentalCoroutinesApi
@HiltViewModel
class ViewPostViewModel
@Inject
constructor(
    application: Application,
    private val postRepository: PostRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ViewPostViewModel"
    }

    private val currentUserUid
        get() = postRepository.currentUser?.uid

    private val _userPosts = MutableLiveData<DataState<List<PostItem>>>()
    val userPosts: LiveData<DataState<List<PostItem>>> get() = _userPosts

    fun getUploadedPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            currentUserUid?.let { uid ->
                postRepository.getPostsByUser(uid).collect {
                    _userPosts.postValue(it)
                }
            }
        }
    }

    fun deletePost(id: String, photoPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.deletePost(id, photoPath)
        }
    }

    fun like(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.likePost(postId)
        }
    }
}