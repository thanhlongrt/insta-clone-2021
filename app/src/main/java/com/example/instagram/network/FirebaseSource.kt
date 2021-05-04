package com.example.instagram.network

import android.content.Context
import android.net.Uri
import com.example.instagram.ImageUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
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

    val photoStorage =
        firebaseStorage.reference
            .child("Photos")

    suspend fun uploadPhoto(context: Context, uri: Uri, path: String): String {
        val imageSize = ImageUtils.getImageSize(context, uri)
        val data = if (imageSize > 1 * 1024 * 1024) ImageUtils.compress(context, uri)
        else ImageUtils.readBytes(context, uri)!!

        return photoStorage.child(path).putBytes(data).await()
            .storage.downloadUrl.await().toString()
    }

    fun savePostData(postData: HashMap<String, Any>): Task<Void> {
        val postId = firebaseDatabase.reference.child("Posts").push().key!!
        postData["post_id"] = postId
        return firebaseDatabase.reference.child("Posts")
            .child(postId)
            .updateChildren(postData)
    }

    fun saveStoryData(storyData: HashMap<String, Any>): Task<Void> {
        val storyId = firebaseDatabase.reference.child("Stories").push().key!!
        storyData["story_id"] = storyId
        return firebaseDatabase.reference.child("Stories")
            .child(storyId)
            .updateChildren(storyData)
    }

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