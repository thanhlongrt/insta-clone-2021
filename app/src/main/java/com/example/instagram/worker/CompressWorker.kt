package com.example.instagram.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.example.instagram.Constants.KEY_COMPRESSED_FILE_URI
import com.example.instagram.Constants.KEY_POST_JSON
import com.example.instagram.Constants.KEY_URI
import com.example.instagram.TypeConverters
import com.iceteck.silicompressorr.SiliCompressor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by Thanh Long Nguyen on 5/11/2021
 */

@HiltWorker
class CompressWorker
@AssistedInject
constructor(
    @Assisted
    context: Context,
    @Assisted
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "MediaCompressWorker"
    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.e(TAG, "doWork: Start compressing")
        try {
            val resourceUri = inputData.getString(KEY_URI)
            Log.e(TAG, "doWork: $resourceUri")
            val postJson = inputData.getString(KEY_POST_JSON)
            val post = TypeConverters.jsonToPost(postJson!!)

            val outputDataBuilder = Data.Builder()
                .putString(KEY_POST_JSON, postJson)

            if (post.is_video) {
//                val uri = compressVideo(resourceUri)
                outputDataBuilder.putString(KEY_COMPRESSED_FILE_URI, resourceUri)
            } else {
                val uri = compressImage(resourceUri)
                outputDataBuilder.putString(KEY_COMPRESSED_FILE_URI, uri.toString())
            }

            Log.e(TAG, "doWork: Success")
            return@coroutineScope Result.success(outputDataBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Fail: ${e.message}")
            return@coroutineScope Result.failure()
        }
    }

    private fun compressImage(resourceUri: String?): Uri? {
        val uri = Uri.parse(resourceUri)
        val outputDir =
            File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "pictures")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val name = "IMG_${System.currentTimeMillis()}.jpg"
        val outputFile = File(outputDir, name)
        var fos: FileOutputStream? = null
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(applicationContext.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)

        } else {
            MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, uri)
        }
        try {
            fos = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos)
        } finally {
            fos?.let {
                try {
                    it.close()
                } catch (e: java.lang.Exception) {

                }
            }
        }
        return Uri.fromFile(outputFile)
    }

    private suspend fun compressVideo(resourceUri: String?): Uri? {
        val outputDir = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "videos"
        )
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputPath =
            withContext(Dispatchers.IO) {
                SiliCompressor.with(applicationContext)
                    .compressVideo(
                        Uri.parse(resourceUri),
                        outputDir.path
                    )
            }
        val outputFile = File(outputPath)
        val uri = Uri.fromFile(outputFile)
        return uri
    }
}