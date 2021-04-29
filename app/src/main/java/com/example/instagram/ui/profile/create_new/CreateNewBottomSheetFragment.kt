package com.example.instagram.ui.profile.create_new

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.TakePhotoFromCameraOrGallery
import com.example.instagram.databinding.FragmentCreateNewBottomSheetBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Thanh Long Nguyen on 4/19/2021
 */

@AndroidEntryPoint
class CreateNewBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        private const val TAG = "CreateBottomSheet"
    }

    private var binding: FragmentCreateNewBottomSheetBinding? = null

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val postViewModel: PostViewModel by viewModels()

    private lateinit var photoStoragePath: String

    private val newPost =
        registerForActivityResult(TakePhotoFromCameraOrGallery()) { uri ->
            Log.e(TAG, "getMedia: uri: $uri")
            uri?.let {
                val bundle = bundleOf("uri" to it)
                getFragmentNavController(
                    R.id.nav_host_fragment
                )?.navigate(
                    R.id.action_createBottomSheetFragment_to_previewImageFragment,
                    bundle
                )
            }
        }

    private val newStory =
        registerForActivityResult(TakePhotoFromCameraOrGallery()) { uri ->
            Log.e(TAG, "newStory: uri: $uri")
            uri?.let {
                photoStoragePath = "IMG_${System.currentTimeMillis()}.jpg"
                postViewModel.upload(it, photoStoragePath)

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateNewBottomSheetBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustNavigationBarIconsColor(view)

        binding?.feedPost?.setOnClickListener {
            newPost.launch(Unit)
        }

        binding?.story?.setOnClickListener {
            newStory.launch(Unit)
        }

        binding?.reel?.setOnClickListener {

        }

        postViewModel.uploadResult.observe(viewLifecycleOwner, {
            when (it.status) {
                SUCCESS -> {
                    val storyData = HashMap<String, Any>()
                    storyData["uid"] = profileViewModel.userLiveData.value!!.data!!.uid
                    storyData["photo_url"] = it.data!!
                    storyData["video_url"] = ""
                    storyData["path"] = photoStoragePath
                    storyData["username"] = profileViewModel.userLiveData.value!!.data!!.username
                    postViewModel.saveStoryData(storyData)
                }
                ERROR -> {

                }
                LOADING -> {
                    displayProgressBar(true)
                }
                IDLE -> {

                }
            }
        })

        postViewModel.saveStoryDataResult.observe(viewLifecycleOwner, {
            when (it.status) {
                SUCCESS -> {
                    displayProgressBar(false)
                    getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
                }
                ERROR -> {
                    displayProgressBar(false)
                    Snackbar.make(view, it.message!!, 5000).show()
                }
                LOADING -> {
                }
                IDLE -> {
                }
            }
        })
    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        activity?.let {
            it.findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (isDisplayed) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun adjustNavigationBarIconsColor(view: View) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            view.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
    }

}