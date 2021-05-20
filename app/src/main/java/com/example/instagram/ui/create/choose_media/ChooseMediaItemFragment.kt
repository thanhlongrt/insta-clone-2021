package com.example.instagram.ui.create.choose_media

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.instagram.utils.Constants.KEY_ACTION
import com.example.instagram.utils.Constants.KEY_URI
import com.example.instagram.utils.Constants.PICK_PHOTO
import com.example.instagram.utils.Constants.PICK_VIDEO
import com.example.instagram.R
import com.example.instagram.databinding.FragmentChoosePhotoBinding
import com.example.instagram.extensions.getFragmentNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 5/18/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChooseMediaItemFragment : Fragment() {

    companion object {
        private const val TAG = "ChooseMediaItemFragment"
    }

    private var binding: FragmentChoosePhotoBinding? = null

    private val chooseMediaViewModel: ChooseMediaViewModel by navGraphViewModels(R.id.nav_create)

    private lateinit var imageAdapter: DeviceImageAdapter

    private var action: String? = null

    private var selectedItem: GalleryMedia? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageAdapter = DeviceImageAdapter(mutableListOf())
        arguments?.let {
            action = it.getString(KEY_ACTION)
            Log.e(TAG, "onCreate: $action")
            when (action) {
                PICK_PHOTO -> {
                    chooseMediaViewModel.loadDeviceImages()
                }
                PICK_VIDEO -> {
                    chooseMediaViewModel.loadDeviceVideos()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChoosePhotoBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.cancelButton?.setOnClickListener {
//            chooseMediaViewModel.clear()
            getFragmentNavController(R.id.nav_host_fragment)?.popBackStack()
        }

        binding?.toolBar?.inflateMenu(R.menu.menu_next)
        binding?.toolBar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_next -> {
                    when (action) {
                        PICK_PHOTO -> {
                            chooseMediaViewModel.selectedMedia.value?.let {
                                Log.e(TAG, "photo uri: ${it.uri}")
                                val bundle = bundleOf(KEY_URI to it.uri)
                                getFragmentNavController(R.id.nav_host_fragment)
                                    ?.navigate(
                                        R.id.action_chooseMediaFragment_to_previewImageFragment,
                                        bundle
                                    )
                            }
                        }
                        PICK_VIDEO -> {
                            chooseMediaViewModel.selectedMedia.value?.let {
                                Log.e(TAG, "video uri: ${it.uri}")
                                val bundle = bundleOf(KEY_URI to it.uri)
                                getFragmentNavController(R.id.nav_host_fragment)
                                    ?.navigate(
                                        R.id.action_chooseMediaFragment_to_previewVideoFragment,
                                        bundle
                                    )
                            }
                        }
                    }
                    true
                }
                else -> {
                    false
                }
            }
        }

        binding?.albumSpinner?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_chooseMediaFragment_to_albumBottomSheetFragment)
        }

        imageAdapter.onItemSelected = { media, position ->
            chooseMediaViewModel.selectMedia(position)
        }

        binding?.imageRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 4)
            adapter = imageAdapter
        }


        chooseMediaViewModel.selectedAlbum.observe(requireActivity()) {
            if (binding != null && it != null) {
                binding!!.albumTitle.text = it.title
                imageAdapter.addAll(it.mediaList)
            }
        }

        chooseMediaViewModel.selectedMedia.observe(requireActivity()) { media ->
            if (binding != null && media != null) {
                Glide.with(view.context)
                    .load(media.uri)
                    .into(binding!!.selectedImage)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}