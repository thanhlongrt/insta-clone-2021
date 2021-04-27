package com.example.instagram.repository

import com.example.instagram.DataState
import com.example.instagram.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class UserRepository
@Inject
constructor(
    private val firebaseSource: FirebaseSource
) {

    fun logout() = firebaseSource.logout()

    val currentFirebaseUser
        get() = firebaseSource.currentFirebaseUser


    fun createUser(email: String, password: String) =
        firebaseSource.createUser(email, password)

    fun login(email: String, password: String) =
        firebaseSource.login(email, password)

    fun saveUserData(uid: String, userData: HashMap<String, Any>) =
        firebaseSource.saveUserData(uid, userData)

    fun userDataReference(uid: String) =
        firebaseSource.userDataReference(uid)

    fun allUserDataReference() =
        firebaseSource.allUsersDataReference()
}