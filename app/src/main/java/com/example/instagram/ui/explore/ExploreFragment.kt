package com.example.instagram.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentExploreBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.view_post.PostGridListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ExploreFragment : Fragment() {

    companion object {
        private const val TAG = "ExploreFragment"
    }

    private var binding: FragmentExploreBinding? = null

    private val exploreViewModel: ExploreViewModel by viewModels()

    private lateinit var gridListAdapter: PostGridListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exploreViewModel.getAllPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentExploreBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers()

        configObservers()
    }

    private fun setupControllers() {
        binding?.searchViewHolder?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_exploreFragment_to_searchFragment)
        }

        val spannedGridLayoutManager = SpannedGridLayoutManager(
            object : SpannedGridLayoutManager.GridSpanLookup {
                override fun getSpanInfo(position: Int): SpannedGridLayoutManager.SpanInfo {
                    return when (position % 18) {
                        1, 9 -> SpannedGridLayoutManager.SpanInfo(2, 2)
                        else -> SpannedGridLayoutManager.SpanInfo(1, 1)
                    }
                }
            }, 3, 1f
        )

        gridListAdapter = PostGridListAdapter(mutableListOf())
        binding?.recyclerView?.apply {
            layoutManager = spannedGridLayoutManager
            adapter = gridListAdapter
        }
    }

    private fun configObservers() {
        exploreViewModel.feedPosts.observe(viewLifecycleOwner) {
            when (it.status) {
                SUCCESS -> {
                    displayProgressBar(false)
                    gridListAdapter.addAll(it.data!!.reversed())
                }
                ERROR -> {
                    displayProgressBar(false)
                }
                LOADING -> {
                    displayProgressBar(true)
                }
            }
        }
    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        activity?.let {
            it.findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (isDisplayed) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}