package com.example.instagram.ui.create.choose_media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.FragmentChoosePhotoBinding
import com.example.instagram.getFragmentNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 5/18/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChoosePhotoFragment : Fragment() {

    companion object {
        private const val TAG = "DeviceImagesFragment"
    }

    private var binding: FragmentChoosePhotoBinding? = null

    private val chooseMediaViewModel: ChooseMediaViewModel by activityViewModels()

    private lateinit var imageAdapter: DeviceImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseMediaViewModel.loadDeviceImages()

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
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        binding?.albumSpinner?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_chooseMediaFragment_to_albumBottomSheetFragment)
        }

        imageAdapter = DeviceImageAdapter(mutableListOf())
        imageAdapter.onItemSelected = { media, position ->
            Glide.with(view.context)
                .load(media.uri)
                .into(binding!!.currentSelectedImage)
            imageAdapter.select(position)
        }

        binding?.imageRecyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 4)
            adapter = imageAdapter
        }


        chooseMediaViewModel.selectedAlbum.observe(requireActivity()) {
            binding?.albumTitle?.text = it.title
        }


        chooseMediaViewModel.mediaFromSelectedAlbum.observe(requireActivity()) { mediaList ->
            if (mediaList.isNotEmpty()) {
                Glide.with(view.context)
                    .load(mediaList.first().uri)
                    .into(binding!!.currentSelectedImage)
                imageAdapter.previousSelectedItemPosition = -1
                imageAdapter.addAll(mediaList)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}