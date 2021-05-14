package com.example.instagram.ui.create

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import com.example.instagram.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Thanh Long Nguyen on 4/13/2021
 */

class TakePhotoContract : ActivityResultContract<Unit, Uri?>() {

    private var mediaUri: Uri? = null

    override fun createIntent(context: Context, input: Unit?): Intent {
        return openImageIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.data ?: mediaUri
    }

    private fun openImageIntent(context: Context): Intent {

        // camera
        val cameraImageCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        mediaUri = createMediaTakenUri(context)
        // write the captured image to a file

        // look for other available intents
        val intents = ArrayList<Intent>()
        val packageManager = context.packageManager

        packageManager.queryIntentActivities(cameraImageCaptureIntent, 0).forEach {
            val finalIntent = Intent(cameraImageCaptureIntent)
            finalIntent.component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
            finalIntent.`package` = it.activityInfo.packageName
            finalIntent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri)
            intents.add(finalIntent)
        }


        // gallery
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"

        val chooser = Intent.createChooser(galleryIntent, "Select Source")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())

        return chooser

    }

    private fun createFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES+"/Instaclone")
            ?: throw IllegalStateException("Dir not found")
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    private fun createMediaTakenUri(context: Context): Uri {
        val file = createFile(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }
}