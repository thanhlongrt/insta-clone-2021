package com.example.instagram.ui.explore.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.firebase_model.User
import com.example.instagram.model.PostItem
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.zip
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
    private val postRepository: PostRepository
) : ViewModel() {
    companion object {
        private const val TAG = "SearchViewModel"
    }

    private val _searchUserResult = MutableLiveData<DataState<List<User>>>()
    val searchUserResult: LiveData<DataState<List<User>>> = _searchUserResult

    private val _otherUserLiveData = MutableLiveData<DataState<User>>()
    val otherUserLiveData: LiveData<DataState<User>> = _otherUserLiveData

    private val _otherUserPosts = MutableLiveData<DataState<List<PostItem>>>()
    val otherUserPosts: LiveData<DataState<List<PostItem>>> = _otherUserPosts

    private val _likeId = MutableLiveData<DataState<String>>()
    val likeIdToDelete : LiveData<DataState<String>> = _likeId

    @ExperimentalCoroutinesApi
    fun getPostById(uid: String) {
        _otherUserPosts.postValue(DataState.loading())
        Log.e(TAG, "getPostById: Loading", )
        viewModelScope.launch {
            postRepository.getPostsById(uid)
                .zip(postRepository.getAllLikes()) { postResult, likeResult ->

                    val likeByPost = likeResult.data!!.groupBy { it.post_id }
                    val posts = postResult.data!!
                    val postItems = posts.map { post ->
                        PostItem(
                            postId = post.post_id,
                            uid = post.uid,
                            avatarUrl = post.avatar_url,
                            userName = post.user_name,
                            photoUrl = post.photo_url,
                            date = post.date_created,
                            caption = post.caption,
                            path = post.path,
                            likes = likeByPost[post.post_id] ?: mutableListOf()
                        )
                    }
                    postItems
                }.catch { e ->
                    _otherUserPosts.postValue(DataState.error(null, e.message!!))
                    Log.e(TAG, "getPostById: Error: ${e.message}")
                }
                .collect {
                    _otherUserPosts.postValue(DataState.success(it))
                    Log.e(TAG, "getPostById: Success", )
                }
        }
    }

    fun getUserData(uid: String) {
        _otherUserLiveData.postValue(DataState.loading())
        Log.e(TAG, "getUserData: Loading")
        userRepository.userDataReference(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    _otherUserLiveData.postValue(DataState.success(user))
                    Log.e(TAG, "onDataChange: getUserData: Success")
                }

                override fun onCancelled(error: DatabaseError) {
                    _otherUserLiveData.postValue(DataState.error(null, error.message))
                    Log.e(TAG, "onDataChange: getUserData: Error")
                }
            })
    }

    fun searchUser(query: String): LiveData<DataState<List<User>>> {
        Log.e(TAG, "searchUser: Searching for: $query")
        _searchUserResult.postValue(DataState.loading())
        userRepository.allUserDataReference()
            .orderByChild("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val results = mutableListOf<User>()
                    for (item in snapshot.children) {
                        val user = item.getValue(User::class.java)!!
                        if (user.uid != userRepository.currentFirebaseUser!!.uid) {
                            results.add(user)
                        }

                    }
                    Log.e(TAG, "onDataChange: searchUser: Success")
                    _searchUserResult.postValue(DataState.success(results))
                }

                override fun onCancelled(error: DatabaseError) {
                    _searchUserResult.postValue(DataState.error(null, error.message))
                }
            })
        return searchUserResult
    }

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

}