package com.example.instagram.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.webkit.MimeTypeMap
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.max

/**
 * Created by Thanh Long Nguyen on 4/15/2021
 */
object ImageUtils {

    fun Bitmap.getCircularBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        // circle configuration
        val circlePaint = Paint().apply { isAntiAlias = true }
        val circleRadius = max(width, height) / 2f

        // output bitmap
        val outputBitmapPaint =
            Paint(circlePaint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
        val outputBounds = Rect(0, 0, width, height)
        val output = Bitmap.createBitmap(width, height, config)

        return Canvas(output).run {
            drawCircle(circleRadius, circleRadius, circleRadius, circlePaint)
            drawBitmap(this@getCircularBitmap, outputBounds, outputBounds, outputBitmapPaint)
            output
        }
    }

    fun compress(context: Context, photoUri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)

        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
        }

        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        return baos.toByteArray()

    }

    fun readBytes(context: Context, uri: Uri): ByteArray? =
        context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }

    fun getImageSize(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            it.moveToFirst()
            val imageSize = cursor.getLong(sizeIndex)
            it.close()
            return imageSize
        }
        return 0
    }

    fun getScreenWidth(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = (context as Activity).windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

            displayMetrics.widthPixels
        }
    }

    fun getScreenHeight(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = (context as Activity).windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())

            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    fun Uri.mimeType(contentResolver: ContentResolver)
            : String? {
        return if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            // get (image/jpeg, video/mp4) from ContentResolver if uri scheme is "content://"
            contentResolver.getType(this)
        } else {
            // get (.jpeg, .mp4) from uri "file://example/example.mp4"
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(toString())
            // turn ".mp4" into "video/mp4"
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.US))
        }
    }

}