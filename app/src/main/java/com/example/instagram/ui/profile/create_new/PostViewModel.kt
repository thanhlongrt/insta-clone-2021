package com.example.instagram.ui.profile.create_new

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
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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

    private val _userPosts = MutableLiveData<DataState<List<PostItem>>>()
    val userPosts: LiveData<DataState<List<PostItem>>>
        get() = _userPosts

    private val _likeId = MutableLiveData<DataState<String>>()
    val likeIdToDelete : LiveData<DataState<String>> = _likeId

    private val _deletePostResult = MutableLiveData<DataState<Boolean>>()
    val deletePostResult: LiveData<DataState<Boolean>> = _deletePostResult

    private val _saveStoryDataResult = MutableLiveData<DataState<Boolean>>()
    val saveStoryDataResult: LiveData<DataState<Boolean>> = _saveStoryDataResult

    val currentUserUid = postRepository.currentUser!!.uid

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
//    fun getPosts() {
//        _postLiveData.postValue(DataState.loading())
//        userPostsListener = postRepository.postDataReference.addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val posts = mutableListOf<Post>()
//                for (item in snapshot.children) {
//                    val post = item.getValue(Post::class.java)!!
//                    if (post.uid == postRepository.currentUser?.uid) {
//                        posts.add(post)
//                    }
//                }
//                Log.e(TAG, "onDataChange: getPosts: ${postRepository.currentUser?.uid}")
//                _postLiveData.postValue(DataState.success(posts))
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                _postLiveData.postValue(DataState.error(null, error.message))
//            }
//        })
//    }

    @ExperimentalCoroutinesApi
    fun getPostById(uid: String) {
        _userPosts.postValue(DataState.loading())
        Log.e(TAG, "getPostById: Loading")
        viewModelScope.launch {
            postRepository.getPostsById(uid)
                .combine(postRepository.getAllLikes()) { postResult, likeResult ->

                    val likeByPost = likeResult.data!!.groupBy { it.post_id }
                    val posts = postResult.data!!
                    val postItems = posts.map { post ->
                        var isLiked = false
                        likeByPost[post.post_id]?.filter {
                            it.uid == currentUserUid && it.post_id == post.post_id
                        }?.size?.let {
                            isLiked = it > 0
                        }
                        PostItem(
                            postId = post.post_id,
                            uid = post.uid,
                            avatarUrl = post.avatar_url,
                            userName = post.user_name,
                            photoUrl = post.photo_url,
                            date = post.date_created,
                            caption = post.caption,
                            path = post.path,
                            likes = likeByPost[post.post_id] ?: mutableListOf(),
                            isLiked = isLiked
                        )
                    }
                    postItems
                }.catch { e ->
                    _userPosts.postValue(DataState.error(null, e.message!!))
                    Log.e(TAG, "getPostById: Error: ${e.message}")
                }
                .collect {
                    _userPosts.postValue(DataState.success(it))
                    Log.e(TAG, "getPostById: Success")
                }
        }
    }

    @ExperimentalCoroutinesApi
    fun getLikeId(uid: String, postId: String){
        _likeId.postValue(DataState.loading())
        Log.e(TAG, "getLikeById: ", )
        viewModelScope.launch {
            postRepository.getLikeById(uid, postId)
                .catch { e ->
                    Log.e(TAG, "getLikeId: ${e.message}", )
                }
                .collect { result ->
                    if (result.data!!.isNotEmpty()){
                        _likeId.postValue(DataState.success(result.data[0].like_id))
                        Log.e(TAG, "getLikeId: Success", )
                    } else {
                        _likeId.postValue(DataState.error(null, "Not found"))
                    }
                }
        }
    }

    fun like(likeData: HashMap<String, Any>) {
        postRepository.like(likeData).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e(TAG, "like: Success: ${likeData["like_id"]}")
            }
        }
    }

    fun unlike(likeId: String) {
        Log.e(TAG, "unlike: $likeId")
        postRepository.unlike(likeId)
    }

    override fun onCleared() {
        super.onCleared()
        userPostsListener?.let { postRepository.postDataReference.removeEventListener(it) }
    }

}