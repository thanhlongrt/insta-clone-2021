package com.example.instagram.network.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.instagram.network.entity.*
import com.example.instagram.utils.ImageUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
@ExperimentalCoroutinesApi
class FirebaseService
@Inject
constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseStorage: FirebaseStorage
) {

    companion object {
        private const val TAG = "FirebaseService"
    }

    // comments
    fun getCommentsByPost(postId: String) =
        callbackFlow<List<Comment>?> {
            val commentListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = snapshot.children.mapNotNull { it.getValue(Comment::class.java) }
                    sendBlocking(comments)
                }

                override fun onCancelled(error: DatabaseError) {
                    sendBlocking(null)
                }
            }
            val commentRef = commentDataReference.child(postId)
            commentRef.addValueEventListener(commentListener)
            awaitClose { commentRef.removeEventListener(commentListener) }
        }.catch { emit(null) }

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

        postDataReference.child(commentData["post_id"].toString())
            .child("comment_count")
            .setValue(ServerValue.increment(1))
    }

    // notifications
    fun uploadFcmToken(token: String): Task<Void>? {
        currentFirebaseUser?.let {
            val data = hashMapOf<String, Any>("token" to token)
            return tokenReference.child(it.uid).updateChildren(data)
        }
        return null
    }

    fun getUserFcmToken(uid: String) = callbackFlow<String?> {
        val tokenRef = tokenReference.child(uid)
        val tokenListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = snapshot.getValue(FcmToken::class.java)!!
                sendBlocking(token.token)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }

        tokenRef.addListenerForSingleValueEvent(tokenListener)
        awaitClose {
            tokenRef.removeEventListener(tokenListener)
        }
    }.catch {
        emit(null)
    }

    fun saveNotification(notification: Notification): Task<Void> {
        val notificationId = notificationReference.child(notification.uid).push().key!!
        val data = notification.toMap()
        data["notification_id"] = notificationId
        return notificationReference.child(notification.uid).child(notificationId)
            .updateChildren(data)
    }

    fun getNotifications() = callbackFlow<List<Notification>?> {
        currentFirebaseUser?.uid?.let { uid ->
            val notificationRef = notificationReference.child(uid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications =
                        snapshot.children.mapNotNull { it.getValue(Notification::class.java) }
                    sendBlocking(notifications)
                }

                override fun onCancelled(error: DatabaseError) {
                    sendBlocking(null)
                }

            }
            notificationRef.addValueEventListener(listener)
            awaitClose { notificationRef.removeEventListener(listener) }
        }
    }.catch {
        emit(null)
    }

    fun followUser(uid: String) {
        userDataReference(uid).child("follower_count").setValue(ServerValue.increment(1))
        val followerData = hashMapOf<String, Any>(currentFirebaseUser?.uid!! to true)
        userDataReference(uid).child("followers").updateChildren(followerData)

        userDataReference(currentFirebaseUser?.uid!!).child("following_count")
            .setValue(ServerValue.increment(1))
        val followingData = hashMapOf<String, Any>(uid to true)
        userDataReference(currentFirebaseUser?.uid!!).child("following")
            .updateChildren(followingData)
    }

    fun unFollow(uid: String) {
        userDataReference(uid).child("follower_count").setValue(ServerValue.increment(-1))
        userDataReference(uid).child("followers").child(currentFirebaseUser!!.uid).setValue(null)

        userDataReference(currentFirebaseUser?.uid!!).child("following_count")
            .setValue(ServerValue.increment(-1))
        userDataReference(currentFirebaseUser?.uid!!).child("following").child(uid)
            .setValue(null)
    }

    // posts
    fun getPostById(postId: String) = callbackFlow<Post?> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postItem = snapshot.getValue(Post::class.java)!!
                sendBlocking(postItem)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }
        val postRef = postDataReference.child(postId)
        postRef.addListenerForSingleValueEvent(postListener)
        awaitClose { postRef.removeEventListener(postListener) }
    }.catch { emit(null) }

    fun getFeedPostsFromFirebase() = callbackFlow<List<Post>?> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = snapshot.children
                    .map { it.getValue(Post::class.java)!! }
                    .filter { it.uid != currentFirebaseUser!!.uid }
                sendBlocking(posts)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }
        postDataReference.addListenerForSingleValueEvent(postListener)
        awaitClose {
            postDataReference.removeEventListener(postListener)
        }
    }

    fun getPostsByUserFromFirebase(uid: String) = callbackFlow<List<Post>?> {
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkPosts = snapshot.children
                    .mapNotNull { it.getValue(Post::class.java)!! }
                    .filter { it.uid == uid }
                sendBlocking(networkPosts)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }
        if (uid == currentFirebaseUser?.uid) {
            postDataReference.addValueEventListener(postListener)
        } else {
            postDataReference.addListenerForSingleValueEvent(postListener)
        }
        awaitClose {
            postDataReference.removeEventListener(postListener)
        }
    }

    fun likePost(postId: String) {
        postDataReference.child(postId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val post = currentData.getValue(Post::class.java)
                    ?: return Transaction.success(currentData)
                try {
                    val uid = currentFirebaseUser!!.uid
                    if (post.likes.contains(uid)) {
                        post.like_count = post.like_count - 1
                        post.likes.remove(uid)
                    } else {
                        post.like_count = post.like_count + 1
                        post.likes[uid] = true
                    }
                    currentData.value = post
                } catch (e: Exception) {
                    Log.e(TAG, "likePost: doTransaction: error: ${e.message}")
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
            }
        })
    }

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

    // story
    fun saveStoryData(storyData: HashMap<String, Any>): Task<Void> {
        val storyId = storyDataReference.push().key!!
        storyData["story_id"] = storyId
        return storyDataReference
            .child(storyId)
            .updateChildren(storyData)
    }

    suspend fun getStoriesFromFirebase() = callbackFlow<List<Story>?> {
        val storyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val networkStories = snapshot.children.mapNotNull {
                    it.getValue(Story::class.java)
                }
                sendBlocking(networkStories)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }
        storyDataReference.addValueEventListener(storyListener)
        awaitClose {
            storyDataReference.removeEventListener(storyListener)
        }
    }

    // user
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

    fun getUserFromFirebase(uid: String) = callbackFlow<User?> {
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!

                sendBlocking(user)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }
        userDataReference(uid).addValueEventListener(userListener)
        awaitClose {
            userDataReference(uid).removeEventListener(userListener)
        }
    }

    fun searchForUser(query: String) = callbackFlow<List<User>?> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<User>()
                for (item in snapshot.children) {
                    val user = item.getValue(User::class.java)!!
                    if (user.uid != currentFirebaseUser?.uid) {
                        results.add(user)
                    }
                }
                sendBlocking(results)
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(null)
            }
        }
        val userRef = allUsersDataReference
            .orderByChild("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
        userRef.addListenerForSingleValueEvent(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }

    fun userDataReference(uid: String) =
        firebaseDatabase.reference
            .child("Users")
            .child(uid)

    private val allUsersDataReference =
        firebaseDatabase.reference
            .child("Users")

    val photoStorage =
        firebaseStorage.reference
            .child("Photos")

    val storageReference = firebaseStorage.reference

    val currentFirebaseUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val postDataReference =
        firebaseDatabase.reference.child("Posts")

    private val storyDataReference =
        firebaseDatabase.reference.child("Stories")

    private val commentDataReference =
        firebaseDatabase.reference.child("Comments")

    private val tokenReference =
        firebaseDatabase.reference.child("FCM Tokens")

    private val notificationReference =
        firebaseDatabase.reference.child("Notifications")
}