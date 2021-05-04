package com.example.instagram.ui.create

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instagram.ImageUtils.getImageSize
import com.example.instagram.R

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */
class PreviewPhotoFragment : Fragment() {

    companion object {
        private const val TAG = "PreviewPhotoFragment"
    }


    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_preview_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uri = arguments?.getParcelable("uri")


        val imageSize = getImageSize(view.context, uri!!)
        Log.e(TAG, "onViewCreated: image size: ${imageSize / 1024}kB")

        Glide.with(view.context)
            .load(uri)
            .into(view.findViewById(R.id.imageView))

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_next, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_next -> {
                val bundle = bundleOf("uri" to uri)
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


}