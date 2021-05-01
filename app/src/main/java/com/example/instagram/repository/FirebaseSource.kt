package com.example.instagram.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
class FirebaseSource
@Inject
constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage
) {

    fun createUser(email: String, password: String) =
        firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun login(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun logout() =
        firebaseAuth.signOut()

    fun saveUserData(uid: String, userData: HashMap<String, Any>) =
        firebaseDatabase.reference
            .child("Users")
            .child(uid)
            .updateChildren(userData)

    fun userDataReference(uid: String) =
        firebaseDatabase.reference
            .child("Users")
            .child(uid)

    fun allUsersDataReference() =
        firebaseDatabase.reference
            .child("Users")

    val profileImageStorage =
        firebaseStorage.reference
            .child("Profile Images")

    val photoStorage =
        firebaseStorage.reference
            .child("Photos")

    fun generatePostId() =
        firebaseDatabase.reference.child("Posts").push().key!!

    fun savePostData(id: String, data: HashMap<String, Any>) =
        firebaseDatabase.reference.child("Posts")
            .child(id)
            .updateChildren(data)

    fun generateStoryId() =
        firebaseDatabase.reference.child("Stories").push().key!!

    fun saveStoryData(id: String, storyData: HashMap<String, Any>): Task<Void> {
        return firebaseDatabase.reference.child("Stories")
            .child(id)
            .updateChildren(storyData)
    }

    fun generateLikeId() =
        firebaseDatabase.reference.child("Likes").push().key!!

    fun like(likeData: HashMap<String, Any>): Task<Void> {
        val id = firebaseDatabase.reference.child("Likes").push().key!!
        likeData["like_id"] = id
        return firebaseDatabase.reference.child("Likes")
            .child(id)
            .updateChildren(likeData)
    }

    fun unlike(likeId: String): Task<Void> {
        return firebaseDatabase.reference.child("Likes")
            .child(likeId)
            .removeValue()
    }


    val currentFirebaseUser: FirebaseUser? get() = firebaseAuth.currentUser

    val postDataReference =
        firebaseDatabase.reference.child("Posts")

    val storyDataReference =
        firebaseDatabase.reference.child("Stories")

    val likeDataReference =
        firebaseDatabase.reference.child("Likes")

}