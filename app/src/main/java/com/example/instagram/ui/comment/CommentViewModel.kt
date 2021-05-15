package com.example.instagram.ui.comment

import androidx.lifecycle.*
import com.example.instagram.Status
import com.example.instagram.model.PostItem
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.Comment
import com.example.instagram.network.entity.Notification
import com.example.instagram.repository.CommentRepository
import com.example.instagram.repository.NotificationRepository
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CommentViewModel"
    }

    val currentUser: LiveData<UserItem?> = userRepository.getUserFlow().asLiveData()

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>>
        get() = _comments

    private val _currentPost = MutableLiveData<PostItem>()
    val currentPost: LiveData<PostItem>
        get() = _currentPost

    val commentEditText = MutableLiveData<String>()

    fun addComment() {
        val post = _currentPost.value
        val user = currentUser.value
        if (user != null && post != null && !commentEditText.value.isNullOrBlank()) {
            viewModelScope.launch() {
                withContext(Dispatchers.IO) { saveComment(user, post, commentEditText.value!!) }
                withContext(Dispatchers.IO) { sendPushNotification(user, post) }
            }
            commentEditText.postValue("")
        }
    }

    private fun saveComment(user: UserItem, post: PostItem, content: String) {
        val comment = Comment(
            uid = user.uid,
            avatar = user.avatarUrl,
            username = user.username,
            content = content,
            post_id = post.postId,
            date_created = System.currentTimeMillis()
        )
        commentRepository.saveCommentData(comment)
    }

    private suspend fun sendPushNotification(user: UserItem, post: PostItem) {
        val notification = Notification(
            uid = post.uid,
            post_id = post.postId,
            title = "Instagram",
            body = "${post.userName}: ${user.username} commented on your post",
            date = System.currentTimeMillis(),
            sender_avatar = user.avatarUrl,
            seen = false
        )
        notificationRepository.sendPushNotification(notification)
    }

    fun getPostById(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.getPostById(postId).collect { postResult ->
                when (postResult.status) {
                    Status.SUCCESS -> {
                        _currentPost.postValue(postResult.data!!)
                    }
                }
            }
        }
    }

    fun getCommentsByPost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            commentRepository.getCommentsByPost(postId).collect { commentsResult ->
                when (commentsResult.status) {
                    Status.SUCCESS -> {
                        _comments.postValue(commentsResult.data!!)
                    }
                }
            }
        }
    }

}