package com.example.instagram.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.instagram.DataState
import com.example.instagram.Status
import com.example.instagram.model.UserItem
import com.example.instagram.repository.PostRepository
import com.example.instagram.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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

    private val _uploadResult = MutableLiveData<DataState<String>>()
    val uploadResult: LiveData<DataState<String>> = _uploadResult

    private val _saveUserDataResult = MutableLiveData<DataState<Boolean>>()
    val saveUserDataResult: LiveData<DataState<Boolean>> = _saveUserDataResult

    private val _userLivaData = MutableLiveData<UserItem>()
    val currentUser: LiveData<UserItem?> =
        userRepository.getUserFlow().asLiveData()

    init {

    }

    fun uploadProfilePicture(photoUri: Uri, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postRepository.uploadImage(getApplication(), photoUri, path).collect {
                _uploadResult.postValue(it)
                Log.e(TAG, "upload: ${it.status}")
            }
        }
    }

    val currentUserUid = postRepository.currentUser!!.uid

    fun logout() {
        userRepository.logout()
    }

    fun updateUserData(userData: HashMap<String, Any>) {
        viewModelScope.launch {
            userRepository.updateUserData(userData).collect {
                _saveUserDataResult.value = it
            }
        }
    }
}