package com.example.instagram.ui.explore.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.Status
import com.example.instagram.model.PostItem
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.Notification
import com.example.instagram.repository.NotificationRepository
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */
@ExperimentalCoroutinesApi
@HiltViewModel
class SearchViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    companion object {
        private const val TAG = "SearchViewModel"
    }

    private val _searchUserResult = MutableLiveData<DataState<List<UserItem>>>()
    val searchUserResult: LiveData<DataState<List<UserItem>>> = _searchUserResult

    private val _otherUserLiveData = MutableLiveData<UserItem>()
    val otherUserLiveData: LiveData<UserItem>
        get() = _otherUserLiveData

    private val _otherUserPosts = MutableLiveData<DataState<List<PostItem>>>()
    val otherUserPosts: LiveData<DataState<List<PostItem>>> = _otherUserPosts

    val currentUserUid = postRepository.currentUser!!.uid

    private val _currentUser = MutableLiveData<UserItem?>()
    val currentUser: LiveData<UserItem?> = _currentUser

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserFlow().collect {
                _currentUser.postValue(it)
            }
        }
    }

    fun follow() {
        val otherUser = otherUserLiveData.value!!
        if (otherUser.isFollowed) {
            userRepository.unfollow(otherUser.uid)
        } else {
            userRepository.followUser(otherUser.uid)
        }
    }

    fun getPostById(uid: String) {
        viewModelScope.launch {
            postRepository.getPostsByUserFromFirebase(uid).collect {
                _otherUserPosts.value = it
            }
        }
    }

    fun getUserDataById(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getUserByUid(uid).collect {
                when (it.status) {
                    Status.SUCCESS -> {
                        _otherUserLiveData.postValue(it.data!!)
                    }
                }
            }
        }
    }

    fun searchForUser(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.searchForUser(query).collect {
                _searchUserResult.postValue(it)
            }
        }
    }

    fun clickLike(postId: String) {
        Log.e(TAG, "like: ")
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.likePost(postId)
        }
    }

    fun sendPushNotification(notification: Notification) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.sendPushNotification(notification)
        }
    }
}