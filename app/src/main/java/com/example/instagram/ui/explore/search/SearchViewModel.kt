package com.example.instagram.ui.explore.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instagram.DataState
import com.example.instagram.firebase_model.Post
import com.example.instagram.firebase_model.User
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */
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

    private val _otherUserPosts = MutableLiveData<DataState<List<Post>>>()
    val otherUserPosts: LiveData<DataState<List<Post>>> = _otherUserPosts

    fun getUserPhotos(uid: String) {
        _otherUserPosts.postValue(DataState.loading())
        Log.e(TAG, "onDataChange: getUserPhotos: Loading")
        postRepository.postDataReference.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val photos = mutableListOf<Post>()
                for (item in snapshot.children) {
                    val photo = item.getValue(Post::class.java)!!
                    if (photo.uid == uid) {
                        photos.add(photo)
                    }
                }
                _otherUserPosts.postValue(DataState.success(photos))
                Log.e(TAG, "onDataChange: getUserPhotos: Success")
            }

            override fun onCancelled(error: DatabaseError) {
                _otherUserPosts.postValue(DataState.error(null, error.message))
                Log.e(TAG, "onDataChange: getUserPhotos: Error")
            }
        })
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

}