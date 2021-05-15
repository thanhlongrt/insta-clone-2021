package com.example.instagram.ui.profile.view_post

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.example.instagram.R
import com.example.instagram.databinding.FragmentPostBottomSheetBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.create.CreateViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostOptionsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "PostBottomSheetFragment"
        fun newInstance() = BottomSheetDialogFragment()
    }

    private var photoId: String? = null

    private var photoPath: String? = null

    private val createViewModel: CreateViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            photoId = it.getString("photo_id")
            photoPath = it.getString("photo_path")
        }
    }

    private var binding: FragmentPostBottomSheetBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPostBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        changeNavigationBarIconsColor(view)

        binding?.copyLink?.setOnClickListener { }

        binding?.edit?.setOnClickListener { }


        var deleteDialog: AlertDialog?
        binding?.delete?.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            deleteDialog = dialogBuilder.setTitle("Delete this post?")
                .setMessage("This will permanently delete your post")
                .setPositiveButton("Delete") { _, _ ->
                    deletePhoto(photoId!!, photoPath!!)

                }
                .setNegativeButton("Don't delete") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            deleteDialog?.show()
        }

    }

    private fun deletePhoto(photoId: String, photoPath: String) {
        createViewModel.deletePost(photoId, photoPath)
        getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
    }

    private fun changeNavigationBarIconsColor(view: View) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            view.windowInsetsController?.setSystemBarsAppearance(
                APPEARANCE_LIGHT_NAVIGATION_BARS,
                APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}