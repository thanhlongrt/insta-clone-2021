package com.example.instagram.network

import android.content.Context
import android.net.Uri
import com.example.instagram.ImageUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
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
        val postId = postDataReference.push().key!!
        postData["post_id"] = postId
        return postDataReference
            .child(postId)
            .updateChildren(postData)
    }

    fun saveStoryData(storyData: HashMap<String, Any>): Task<Void> {
        val storyId = storyDataReference.push().key!!
        storyData["story_id"] = storyId
        return storyDataReference
            .child(storyId)
            .updateChildren(storyData)
    }

    fun saveCommentData(commentData: HashMap<String, Any>) {
        val commentId =
            firebaseDatabase.reference
                .child("Comments")
                .child(commentData["postId"].toString())
                .push().key!!
        commentData["comment_id"] = commentId
        commentDataReference
            .child(commentData["post_id"].toString())
            .child(commentId)
            .updateChildren(commentData)

//        postDataReference.child(commentData["post_id"].toString())
//            .child("comment_count")
//            .setValue(ServerValue.increment(1))
    }

    val currentFirebaseUser: FirebaseUser? get() = firebaseAuth.currentUser

    val postDataReference =
        firebaseDatabase.reference.child("Posts")

    val storyDataReference =
        firebaseDatabase.reference.child("Stories")

    val commentDataReference =
        firebaseDatabase.reference.child("Comments")
}