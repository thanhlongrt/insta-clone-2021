package com.example.instagram.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.DataState
import com.example.instagram.firebase_model.User
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import com.example.instagram.ui.profile.create_new.PostViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/13/2021
 */

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

    private val _userLivaData = MutableLiveData<DataState<User>>()
    val userLiveData: LiveData<DataState<User>> = _userLivaData

    private val _saveUserDataResult = MutableLiveData<DataState<Boolean>>()
    val saveUserDataResult: LiveData<DataState<Boolean>> = _saveUserDataResult

    private val _uploadResult = MutableLiveData<DataState<String>>()
    val uploadResult: LiveData<DataState<String>> = _uploadResult

    fun logout() {
        userRepository.logout()
    }

    fun getCurrentUserData() {
        Log.e(TAG, "getCurrentUserData: Loading", )
        userRepository.userDataReference(userRepository.currentFirebaseUser?.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)!!
                    _userLivaData.postValue(DataState.success(user))
                    Log.e(TAG, "onDataChange: getCurrentUserData: Success")
                    Log.e(TAG, "onDataChange: getCurrentUserData: ${user.uid}", )
                }

                override fun onCancelled(error: DatabaseError) {
                    _userLivaData.postValue(DataState.error(null, error.message))
                    Log.e(TAG, "onCancelled: getCurrentUserData: Error")
                }
            })
    }

    fun saveUserData(userData: HashMap<String, Any>) {
        _saveUserDataResult.postValue(DataState.loading())
        Log.e(TAG, "saveUserData: Loading...")

        val uid = userRepository.currentFirebaseUser!!.uid

        userRepository.saveUserData(uid, userData).addOnCompleteListener {
            if (it.isSuccessful) {
                _saveUserDataResult.postValue(DataState.success(true))
                Log.e(TAG, "saveUserData: Success")
            } else {
                _saveUserDataResult.postValue(DataState.error(false, it.exception?.message!!))
                Log.e(TAG, "saveUserData: Error")
            }
        }
    }

    fun upload(photoUri: Uri, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(TAG, "upload: Loading...")
            _uploadResult.postValue(DataState.loading())
            try {
                val url = postRepository.uploadPhoto(getApplication(), photoUri, path)
                Log.e(TAG, "upload: Success")
                Log.e(TAG, "upload: url: $url")
                _uploadResult.postValue(DataState.success(url))
            } catch (e: Exception) {
                Log.e(PostViewModel.TAG, "upload: Error: ${e.message.toString()}")
                _uploadResult.postValue(DataState.error(null, e.message.toString()))
            }
        }
    }


}