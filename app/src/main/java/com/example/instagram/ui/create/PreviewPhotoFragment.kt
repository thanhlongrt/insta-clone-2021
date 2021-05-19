package com.example.instagram.ui.create

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instagram.Constants.KEY_URI
import com.example.instagram.ImageUtils.getImageSize
import com.example.instagram.R
import com.example.instagram.databinding.FragmentPreviewPhotoBinding
import com.example.instagram.getFragmentNavController

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class PreviewPhotoFragment : Fragment() {

    companion object {
        private const val TAG = "PreviewPhotoFragment"
    }

    private var binding: FragmentPreviewPhotoBinding? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        uri = arguments?.getParcelable(KEY_URI)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreviewPhotoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageSize = getImageSize(view.context, uri!!)
        Log.e(TAG, "onViewCreated: image size: ${imageSize / 1024}kB")

        setupControllers(view)

    }

    private fun setupControllers(view: View) {
        binding?.toolBar?.inflateMenu(R.menu.menu_next)
        binding?.toolBar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_next -> {
                    val bundle = bundleOf(KEY_URI to uri)
                    findNavController().navigate(
                        R.id.action_previewImageFragment_to_createNewPostFragment,
                        bundle
                    )
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
            .into(binding!!.imageView)
    }

}