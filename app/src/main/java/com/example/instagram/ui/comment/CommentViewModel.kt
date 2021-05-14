package com.example.instagram.ui.comment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.network.entity.Comment
import com.example.instagram.network.entity.Notification
import com.example.instagram.repository.CommentRepository
import com.example.instagram.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/6/2021
 */

@ExperimentalCoroutinesApi
@HiltViewModel
class CommentViewModel
@Inject
constructor(
    private val commentRepository: CommentRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CommentViewModel"
    }

    private val _comments = MutableLiveData<DataState<List<Comment>>>()
    val comments: LiveData<DataState<List<Comment>>>
        get() = _comments

    fun addComment(commentData: HashMap<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            commentRepository.addComment(commentData)
        }
    }

    fun getCommentsByPost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            commentRepository.getCommentsFromFirebaseByPost(postId).collect {
                _comments.postValue(it)
//                Log.e(TAG, "getCommentsByPost: ${it.status}")
            }
        }
    }

    fun sendPushNotification(notification: Notification) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationRepository.sendPushNotification(notification)
        }
    }

}