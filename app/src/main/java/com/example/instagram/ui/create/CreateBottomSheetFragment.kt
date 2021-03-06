package com.example.instagram.ui.create

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.example.instagram.utils.Constants.KEY_ACTION
import com.example.instagram.utils.Constants.KEY_URI
import com.example.instagram.utils.Constants.PICK_PHOTO
import com.example.instagram.utils.Constants.PICK_VIDEO
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCreateNewBottomSheetBinding
import com.example.instagram.extensions.getFragmentNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/19/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CreateBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "CreateBottomSheet"
    }

    private var binding: FragmentCreateNewBottomSheetBinding? = null

    private val newPhoto =
        registerForActivityResult(TakePhotoContract()) { uri ->
            Log.e(TAG, "newPhoto: uri: $uri")
            uri?.let {
                val bundle = bundleOf(KEY_URI to it)
                getFragmentNavController(
                    R.id.nav_host_fragment
                )?.navigate(
                    R.id.action_createBottomSheetFragment_to_previewImageFragment,
                    bundle
                )
            }
        }

    private val newStory =
        registerForActivityResult(TakePhotoContract()) { uri ->
            Log.e(TAG, "newStory: uri: $uri")
            uri?.let {

                val bundle = bundleOf(KEY_URI to it)
                getFragmentNavController(
                    R.id.nav_host_fragment
                )?.navigate(
                    R.id.action_createBottomSheetFragment_to_addNewStoryFragment,
                    bundle
                )
            }
        }

    private val newVideo =
        registerForActivityResult(TakeVideoContract()) { uri ->
            Log.e(TAG, "newVideo: uri: $uri")
            uri?.let {
                val bundle = bundleOf(KEY_URI to uri)
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                    R.id.action_createBottomSheetFragment_to_previewVideoFragment,
                    bundle
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askPermissions()
    }

    private fun askPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val notGrantedPermissions = arrayListOf<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                notGrantedPermissions.add(permission)
            }
        }

        if (notGrantedPermissions.isNotEmpty()) {
            val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                    for (entry in result) {
                        if (!entry.value) {
                            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
                        }
                    }
                }
            requestPermissionLauncher.launch(notGrantedPermissions.toTypedArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateNewBottomSheetBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adjustNavigationBarIconsColor(view)

        binding?.feedPost?.setOnClickListener {
//            newPhoto.launch(Unit)
            val bundle = bundleOf(KEY_ACTION to PICK_PHOTO)
            getFragmentNavController(R.id.nav_host_fragment)
                ?.navigate(
                    R.id.action_createBottomSheetFragment_to_chooseMediaFragment,
                    bundle
                )
        }

        binding?.story?.setOnClickListener {
            newStory.launch(Unit)
        }

        binding?.reel?.setOnClickListener {
//            newVideo.launch(Unit)
            val bundle = bundleOf(KEY_ACTION to PICK_VIDEO)
            getFragmentNavController(R.id.nav_host_fragment)
                ?.navigate(
                    R.id.action_createBottomSheetFragment_to_chooseMediaFragment,
                    bundle
                )
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