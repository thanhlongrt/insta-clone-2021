package com.example.instagram.repository

import android.util.Log
import com.example.instagram.DataState
import com.example.instagram.Status
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.User
import com.example.instagram.network.entity.UserNetworkMapper
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.dao.UserDao
import com.example.instagram.room.entity.UserCacheMapper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject


@ExperimentalCoroutinesApi
class UserRepository
@Inject
constructor(
    private val firebaseService: FirebaseService,
    private val stringKeyValueDao: StringKeyValueDao,
    private val userDao: UserDao,
    private val userNetworkMapper: UserNetworkMapper,
    private val userCacheMapper: UserCacheMapper
) {

    companion object {
        private const val TAG = "UserRepository"
    }

    fun logout() = firebaseService.logout()

    val currentUser
        get() = firebaseService.currentFirebaseUser


    suspend fun createUser(email: String, password: String) = callbackFlow<DataState<Boolean>> {
        firebaseService.createUser(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                sendBlocking(DataState.success(true))
            } else {
                sendBlocking(DataState.error(false, it.exception?.message))
            }
        }
        awaitClose { }
    }.onStart {
        emit(DataState.loading())
    }.catch {
        emit(DataState.error(false, it.message))
    }


    suspend fun login(email: String, password: String) = callbackFlow<DataState<Boolean>> {
        firebaseService.login(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                sendBlocking(DataState.success(true))
            } else {
                sendBlocking(DataState.error(false, it.exception?.message))
            }
        }
        awaitClose { }
    }.onStart {
        emit(DataState.loading())
    }.catch {
        emit(DataState.error(false, it.message))
    }


    suspend fun saveUserData(uid: String, userItem: UserItem) {
        val user = userNetworkMapper.fromModel(userItem)
        firebaseService.saveUserData(uid, user.toMap())
        val userCache = userCacheMapper.fromModel(userItem)
        userDao.insertUser(userCache)
    }

    fun allUserDataReference() =
        firebaseService.allUsersDataReference()

    fun getUserDataById(uid: String): Flow<DataState<UserItem>> = flow {
        getUserFromFirebase(uid)
            .collect {
                emit(it)
            }
    }

    private fun getUserFromFirebase(uid: String) = callbackFlow<DataState<UserItem>> {
        Log.e(TAG, "getUserFromFirebase: ")
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                val userItem = userNetworkMapper.fromEntity(user)
                sendBlocking(DataState.success(userItem))
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(DataState.error(null, error.message))
            }
        }
        firebaseService.userDataReference(uid).addListenerForSingleValueEvent(userListener)
        awaitClose {
            firebaseService.userDataReference(uid).removeEventListener(userListener)
        }
    }.catch { e ->
        emit(DataState.error(null, e.message))
    }

    fun updateUserData(userData: HashMap<String, Any>): Flow<DataState<Boolean>> = callbackFlow {
        firebaseService.saveUserData(currentUser!!.uid, userData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    this.sendBlocking(DataState.success(true))
                } else {
                    this.sendBlocking(DataState.error(null, it.exception?.message))
                }
            }
        awaitClose {
        }
    }.onStart {
        emit(DataState.loading())
    }

    suspend fun getUser() =
        withContext(Dispatchers.IO) {
            userDao.getUser()?.let { userCacheMapper.fromEntity(it) }
        }

    fun getUserFlow() =
        userDao.getUserFlow().map { userCache ->
            val userItem = userCache?.let { it -> userCacheMapper.fromEntity(it) }
            userItem
        }

    suspend fun getCurrentUserFromFirebase() = callbackFlow<DataState<UserItem>> {
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                val userItem = userNetworkMapper.fromEntity(user)
                sendBlocking(DataState.success(userItem))
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(DataState.error(null, error.message))
            }
        }
        val userRef = firebaseService.userDataReference(currentUser!!.uid)
        userRef.addValueEventListener(userListener)
        awaitClose {
            userRef.removeEventListener(userListener)
        }
    }.catch { e ->
        emit(DataState.error(null, e.message))
    }.collect {
        if (it.status == Status.SUCCESS) {
            val userCache = userCacheMapper.fromModel(it.data!!)
            userDao.deleteAllAndInsert(userCache)
        }
    }

    fun searchUserFlow(query: String) = callbackFlow<DataState<List<User>>> {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<User>()
                for (item in snapshot.children) {
                    val user = item.getValue(User::class.java)!!
                    if (user.uid != currentUser!!.uid) {
                        results.add(user)
                    }
                }
                sendBlocking(DataState.success(results))
            }

            override fun onCancelled(error: DatabaseError) {
                sendBlocking(DataState.error(null, error.message))
            }
        }

        val userRef = firebaseService.allUsersDataReference()
            .orderByChild("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
        userRef.addListenerForSingleValueEvent(listener)
        awaitClose { userRef.removeEventListener(listener) }

    }.catch { emit(DataState.error(null, it.message)) }
        .onStart { emit(DataState.loading()) }
}