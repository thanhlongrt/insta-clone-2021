package com.example.instagram.ui.profile.view_post

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.example.instagram.R
import com.example.instagram.databinding.FragmentPostBottomSheetBinding
import com.example.instagram.extensions.getFragmentNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
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

    private var postId: String? = null

    private var mediaPath: String? = null

    private val viewPostViewModel: ViewPostViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("post_id")
            mediaPath = it.getString("path")
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
            val view = layoutInflater.inflate(R.layout.dialog_delete, null)
            deleteDialog = dialogBuilder
                .setView(view)
//                .setTitle("Delete this post?")
//                .setMessage("This will permanently delete your post")
//                .setPositiveButton("Delete") { _, _ ->
//                    deletePhoto(postId!!, mediaPath!!)
//
//                }
//                .setNegativeButton("Don't delete") { dialog, _ ->
//                    dialog.dismiss()
//                }
                .create()
            deleteDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            view.findViewById<MaterialButton>(R.id.deleteButton).setOnClickListener {
                deletePhoto(postId!!, mediaPath!!)
                deleteDialog?.dismiss()
            }
            view.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
                deleteDialog?.dismiss()
            }
            deleteDialog?.show()
        }

    }

    private fun deletePhoto(photoId: String, photoPath: String) {
        viewPostViewModel.deletePost(photoId, photoPath)
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