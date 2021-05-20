package com.example.instagram.ui.create.choose_media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.databinding.FragmentAlbumBottomSheetBinding
import com.example.instagram.extensions.getFragmentNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Thanh Long Nguyen on 5/19/2021
 */

@AndroidEntryPoint
class AlbumBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "AlbumBottomSheet"
    }

    private var binding: FragmentAlbumBottomSheetBinding? = null

    private val chooseMediaViewModel: ChooseMediaViewModel by navGraphViewModels(R.id.nav_create)

    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlbumBottomSheetBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumAdapter = AlbumAdapter(mutableListOf())
        albumAdapter.onItemClick = { albumId ->
            chooseMediaViewModel.selectAlbum(albumId)
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        binding?.albumRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = albumAdapter
            setHasFixedSize(true)
        }

        chooseMediaViewModel.albumList.observe(requireActivity()) {
            if (it != null) {
                albumAdapter.addAll(it)
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}