package com.example.instagram.ui.create.choose_media

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.MyApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 5/18/2021
 */

@HiltViewModel
class ChooseMediaViewModel
@Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ChooseMediaViewModel"
        val DEFAULT_ALBUM = Album("galleryId", "Gallery")
    }

    private val allMedias: MutableLiveData<List<GalleryMedia>> = MutableLiveData(listOf())

    val albumList: MutableLiveData<List<Album>> = MutableLiveData(listOf(DEFAULT_ALBUM))

    var selectedAlbum: MutableLiveData<Album> = MutableLiveData(DEFAULT_ALBUM)

    val mediaFromSelectedAlbum: MutableLiveData<List<GalleryMedia>> = MutableLiveData(listOf())

    fun selectAlbum(albumId: String) {
        if (albumId == selectedAlbum.value!!.id) return
        if (albumId == DEFAULT_ALBUM.id) {
            selectedAlbum.value = albumList.value!!.first()
            mediaFromSelectedAlbum.value = allMedias.value
        } else {
            selectedAlbum.value = albumList.value!!.firstOrNull { it.id == albumId } ?: return
            val mediaByAlbum = allMedias.value!!.groupBy { it.albumId }
            mediaFromSelectedAlbum.value = mediaByAlbum[albumId]!!.toList()
        }
        allMedias.value?.forEach { it.isSelected = false }

    }

    fun loadDeviceImages() {
        viewModelScope.launch(Dispatchers.Default) {
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )

            val selection: String? = null
            val selectionArgs: Array<String>? = null

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val contentResolver = getApplication<MyApplication>().contentResolver


            val query = contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )


            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

                val foundAlbums = HashMap<String, Album>()
                val photosFromGallery = mutableListOf<GalleryMedia>()

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val bucketId = cursor.getInt(bucketIdColumn).toString()
                    val bucketName = cursor.getString(bucketNameColumn)
                    val contentUri: Uri =
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                    foundAlbums[bucketId] = Album(bucketId, bucketName)

                    photosFromGallery.add(
                        GalleryMedia(
                            uri = contentUri,
                            albumId = bucketId,
                            albumTitle = bucketName,
                            isVideo = false
                        )
                    )
                }

                val foundAlbumList = foundAlbums.values.toMutableList()
                foundAlbumList.add(0, DEFAULT_ALBUM)
                allMedias.postValue(photosFromGallery)
                mediaFromSelectedAlbum.postValue(photosFromGallery)
                albumList.postValue(foundAlbumList.toList())
            }
        }
    }

    fun loadDeviceVideos() {
        viewModelScope.launch(Dispatchers.Default) {
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
            )

            val selection: String? = null
            val selectionArgs: Array<String>? = null

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            val contentResolver = getApplication<MyApplication>().contentResolver


            val query = contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
                val bucketColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

                val foundAlbumsMap = HashMap<String, Album>()
                val videosFromGallery = mutableListOf<GalleryMedia>()

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val bucketId = cursor.getInt(bucketIdColumn).toString()
                    val bucketName = cursor.getString(bucketColumn)
                    val contentUri: Uri =
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                    foundAlbumsMap[bucketId] = Album(bucketId, bucketName)
                    videosFromGallery.add(
                        GalleryMedia(
                            contentUri,
                            bucketId,
                            bucketName,
                            isVideo = true
                        )
                    )
                }
            }
        }
    }


}