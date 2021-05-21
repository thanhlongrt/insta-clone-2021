package com.example.instagram.ui.create

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.example.instagram.utils.Constants.KEY_IS_VIDEO
import com.example.instagram.utils.Constants.KEY_URI
import com.example.instagram.utils.ImageUtils.mimeType
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCreateNewPostBinding
import com.example.instagram.extensions.getFragmentNavController
import com.example.instagram.network.entity.Post
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CreateNewPostFragment : Fragment() {

    companion object {
        private const val TAG = "CreateNewPostFragment"
    }

    private var binding: FragmentCreateNewPostBinding? = null

    private val createViewModel: CreateViewModel by viewModels()

    private var uri: Uri? = null
    private var isVideo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            uri = it.getParcelable(KEY_URI)
            isVideo = it.getBoolean(KEY_IS_VIDEO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateNewPostBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configObservers(view)

        setupControllers(view)

//        lifecycleScope.launch(Dispatchers.IO) {
//            addPhotoToGallery(uri!!)
//        }

    }

    private fun configObservers(view: View) {
        createViewModel.currentUser.observe(viewLifecycleOwner, { user ->
            if (user != null) {
                Glide.with(view.context)
                    .load(user.avatarUrl)
                    .into(binding?.avatar!!)
            }
        })
    }

    private fun setupControllers(view: View) {
        binding?.toolBar?.inflateMenu(R.menu.menu_done)
        binding?.toolBar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_done -> {
                    val user = createViewModel.currentUser.value
                    if (user != null && uri != null) {
                        val storagePath: String = if (isVideo) {
                            "${user.uid}/VIDEO_${System.currentTimeMillis()}.mp4"
                        } else {
                            "${user.uid}/IMG_${System.currentTimeMillis()}.jpg"
                        }
                        val post = Post(
                            uid = user.uid,
                            avatar_url = user.avatarUrl,
                            user_name = user.username,
                            date_created = System.currentTimeMillis(),
                            caption = binding?.captionEditText?.text.toString(),
                            path = storagePath,
                            like_count = 0,
                            comment_count = 0,
                            is_video = isVideo
                        )
                        createViewModel.savePostData(uri!!, post)

                        getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                            R.id.action_createNewPostFragment_to_createBottomSheetFragment,
                        )
                    }
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }

        binding?.backButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        Glide.with(view.context)
            .load(uri)
            .into(binding?.imageView!!)
    }


    private fun addPhotoToGallery(inputUri: Uri) {
        val contentResolver = context?.contentResolver!!

        val relativeLocation = Environment.DIRECTORY_PICTURES + "/Instaclone/"
        val bitmap = Glide.with(requireContext())
            .asBitmap()
            .load(inputUri)
            .submit().get()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.TITLE, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.DESCRIPTION, "")
            put(MediaStore.MediaColumns.MIME_TYPE, inputUri.mimeType(contentResolver))
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }


        val photoUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            photoUri?.let {
                val stream = contentResolver.openOutputStream(it)
                stream?.let { outputStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            contentResolver.update(photoUri, contentValues, null, null)
                        }
                    } else {
                        throw IOException("Failed to compress bitmap.")
                    }
                } ?: throw IOException("Failed to get output stream")
            } ?: throw IOException("Failed to create new MediaStore record")
        } catch (e: Exception) {
            if (photoUri != null) {
                contentResolver.delete(photoUri, null, null)
            }
            Log.e(TAG, "addPhotoToGallery: ${e.message}")
            throw IOException(e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}