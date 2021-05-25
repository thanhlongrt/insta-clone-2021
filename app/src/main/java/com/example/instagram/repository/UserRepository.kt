package com.example.instagram.repository

import com.example.instagram.DataState
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.UserNetworkMapper
import com.example.instagram.network.firebase.FirebaseService
import com.example.instagram.room.dao.StringKeyValueDao
import com.example.instagram.room.dao.UserDao
import com.example.instagram.room.entity.UserCacheMapper
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

    val currentUser
        get() = firebaseService.currentFirebaseUser

    fun followUser(uid: String) {
        firebaseService.followUser(uid)
    }

    fun unfollow(uid: String) {
        firebaseService.unFollow(uid)
    }

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

    fun logout() = firebaseService.logout()

    suspend fun saveUserData(uid: String, userItem: UserItem) {
        val user = userNetworkMapper.fromModel(userItem)
        firebaseService.saveUserData(uid, user.toMap())
        val userCache = userCacheMapper.fromModel(userItem)
        userDao.insertUser(userCache)
    }

    fun getUserByUid(uid: String) =
        firebaseService.getUserFromFirebase(uid).map { result ->
            val userItem = userNetworkMapper.fromEntity(result!!)
            userItem.isFollowed = userItem.followers.contains(currentUser!!.uid)
            DataState.success(userItem)
        }.onStart {
            emit(DataState.loading())
        }.catch { e ->
            emit(DataState.error(null, e.message))
        }

    suspend fun getCurrentUserFromFirebase() =
        currentUser?.uid?.let {
            firebaseService.getUserFromFirebase(it).map { result ->
                val userItem = userNetworkMapper.fromEntity(result!!)
                val userCache = userCacheMapper.fromModel(userItem)
                userDao.deleteAllAndInsert(userCache)
                return@map DataState.success(userItem)
            }.onStart {
                emit(DataState.loading())
            }.catch { e ->
                emit(DataState.error(null, e.message))
            }
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

    suspend fun getUserCache() =
        withContext(Dispatchers.IO) {
            userDao.getUser()?.let { userCacheMapper.fromEntity(it) }
        }

    fun getUserFlow() =
        userDao.getUserFlow().map { userCache ->
            val userItem = userCache?.let { it -> userCacheMapper.fromEntity(it) }
            userItem
        }

    fun searchForUser(query: String) =
        firebaseService.searchForUser(query).map { result ->
            val userItemResult = result!!.map { userNetworkMapper.fromEntity(it) }
            return@map DataState.success(userItemResult)
        }.catch {
            emit(DataState.error(null, it.message))
        }.onStart { emit(DataState.loading()) }
}