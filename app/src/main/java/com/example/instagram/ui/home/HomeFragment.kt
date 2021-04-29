package com.example.instagram.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.getFragmentNavController

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeViewModel.getStoryData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var count = 0
        binding?.storyButton?.setOnClickListener {
            count++
            binding?.storyButton?.setText("Story: $count")
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_homeFragment_to_storyFragment)
        }

        val storyAdapter = StoryListAdapter(mutableListOf())
        storyAdapter.onItemClick = { position ->
            val bundle = bundleOf("position" to position)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_homeFragment_to_storyFragment,
                bundle
            )
        }
        binding?.storyList?.apply {
            layoutManager = LinearLayoutManager(
                view.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = storyAdapter
        }

        homeViewModel.storiesLiveData.observe(requireActivity()) {
            when (it.status) {
                SUCCESS -> {
                    storyAdapter.addAll(it.data!!)
                }
                ERROR -> {

                }
                LOADING -> {

                }
                IDLE -> {

                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}