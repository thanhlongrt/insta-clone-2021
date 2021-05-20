package com.example.instagram.ui.create.choose_media

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.instagram.ui.main.MyApplication
import com.example.instagram.extensions.notifyObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        private val DEFAULT_PHOTO_ALBUM = Album("default_photo_album_id", "Gallery")
        private val DEFAULT_VIDEO_ALBUM = Album("default_video_album_id", "Gallery")
    }

    val albumList: MutableLiveData<List<Album>> = MutableLiveData()

    var selectedAlbum: MutableLiveData<Album> = MutableLiveData()

    var selectedMedia: MutableLiveData<GalleryMedia> = MutableLiveData()

    var previousSelectedMediaPosition: Int? = null

    fun clear(){
        selectedMedia.value = null
        selectedAlbum.value = null
        previousSelectedMediaPosition = null
        albumList.value = null
    }

    fun selectMedia(position: Int) {
        Log.e(TAG, "selectMedia: ${selectedAlbum.value}")
        if (selectedAlbum.value == null) return
        if (previousSelectedMediaPosition != null &&
            previousSelectedMediaPosition!! < selectedAlbum.value!!.mediaList.size
        ) {
            selectedAlbum.value!!.mediaList[previousSelectedMediaPosition!!].isSelected = false
        }
        selectedAlbum.value!!.mediaList[position].isSelected = true
        selectedMedia.value = selectedAlbum.value!!.mediaList[position]
        previousSelectedMediaPosition = position

        // notify observer that some items in this album are selected/unselected
        selectedAlbum.notifyObserver()
    }

    fun selectAlbum(albumId: String) {
        if (albumId == selectedAlbum.value?.id) return

        // unselect all items before switch to new album
        if (selectedAlbum.value != null) {
            selectedAlbum.value!!.mediaList.forEach { it.isSelected = false }
        }

        selectedAlbum.value = albumList.value?.first { it.id == albumId }
        selectMedia(0) // select first item as default

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

                val albumsFromGallery = HashMap<String, Album>()
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
                    albumsFromGallery[bucketId] = Album(bucketId, bucketName)

                    photosFromGallery.add(
                        GalleryMedia(
                            uri = contentUri,
                            albumId = bucketId,
                            albumTitle = bucketName,
                            isVideo = false
                        )
                    )
                }
                Log.e(TAG, "loadDeviceImages: photosFromGallery: ${photosFromGallery.size}")

                // add photo into its album
                photosFromGallery.forEach {
                    albumsFromGallery[it.albumId]!!.mediaList.add(it)
                }

                // add all photos into DEFAULT ALBUM
                DEFAULT_PHOTO_ALBUM.mediaList.clear()
                DEFAULT_PHOTO_ALBUM.mediaList.addAll(photosFromGallery)

                // add default album which contains all photos
                val albumFromGalleryList = albumsFromGallery.values.toMutableList()
                albumFromGalleryList.add(0, DEFAULT_PHOTO_ALBUM)

                withContext(Dispatchers.Main) {
                    albumList.value = albumFromGalleryList.toList()
                    selectAlbum(DEFAULT_PHOTO_ALBUM.id)
                }
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

                val albumFromGallery = HashMap<String, Album>()
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

                    albumFromGallery[bucketId] = Album(bucketId, bucketName)
                    videosFromGallery.add(
                        GalleryMedia(
                            contentUri,
                            bucketId,
                            bucketName,
                            isVideo = true
                        )
                    )
                }
                Log.e(TAG, "loadDeviceVideos: videoFromGallery: ${videosFromGallery.size}")

                // add video into its album
                videosFromGallery.forEach {
                    albumFromGallery[it.albumId]!!.mediaList.add(it)
                }

                // add all photos into DEFAULT ALBUM
                DEFAULT_VIDEO_ALBUM.mediaList.clear()
                DEFAULT_VIDEO_ALBUM.mediaList.addAll(videosFromGallery)

                // add default album which contains all photos
                val albumFromGalleryList = albumFromGallery.values.toMutableList()
                albumFromGalleryList.add(
                    0,
                    DEFAULT_VIDEO_ALBUM
                ) // add default album which contains all photos

                withContext(Dispatchers.Main) {
                    albumList.value = albumFromGalleryList
                    selectAlbum(DEFAULT_VIDEO_ALBUM.id)
                }
            }
        }
    }


}