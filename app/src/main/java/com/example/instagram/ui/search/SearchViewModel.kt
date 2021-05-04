package com.example.instagram.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.model.PostItem
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.User
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
    private val postRepository: PostRepository
) : ViewModel() {
    companion object {
        private const val TAG = "SearchViewModel"
    }

    private val _searchUserResult = MutableLiveData<DataState<List<User>>>()
    val searchUserResult: LiveData<DataState<List<User>>> = _searchUserResult

    private val _otherUserLiveData = MutableLiveData<DataState<UserItem>>()
    val otherUserLiveData: LiveData<DataState<UserItem>> = _otherUserLiveData

    private val _otherUserPosts = MutableLiveData<DataState<List<PostItem>>>()
    val otherUserPosts: LiveData<DataState<List<PostItem>>> = _otherUserPosts

    val currentUserUid = postRepository.currentUser!!.uid

    @ExperimentalCoroutinesApi
    fun getPostById(uid: String) {
        viewModelScope.launch {
            postRepository.getPostsByUserWithLikes(uid)
                .collect {
                    _otherUserPosts.value = it
                    Log.e(TAG, "getPostById: ${it.status}")
                }
        }
    }

    fun getUserData(uid: String) {
        _otherUserLiveData.postValue(DataState.loading())
        viewModelScope.launch {
            userRepository.getUser(uid)
                .collect {
                    _otherUserLiveData.value = it
                    Log.e(TAG, "getUserData: ${it.status}")
                }
        }

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

    fun like(likeData: HashMap<String, Any>) {
        Log.e(TAG, "like: ")
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.like(likeData)
        }
    }

    fun unlike(uid: String, postId: String) {
        Log.e(TAG, "unlike:")
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.unlike(uid, postId)
        }
    }

}