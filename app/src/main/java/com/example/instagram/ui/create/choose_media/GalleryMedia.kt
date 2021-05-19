package com.example.instagram.ui.create.choose_media

import android.net.Uri

/**
 * Created by Thanh Long Nguyen on 5/18/2021
 */
class GalleryMedia(
    val uri: Uri,
    val albumId: String = "",
    val albumTitle: String = "",
    val isVideo: Boolean = false,
    var isSelected: Boolean = false
) {
}