package com.example.instagram.repository

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