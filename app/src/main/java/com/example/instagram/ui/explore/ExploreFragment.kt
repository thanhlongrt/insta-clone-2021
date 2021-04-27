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
import com.example.instagram.ui.profile.user_post.PostGridListAdapter
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@AndroidEntryPoint
class ExploreFragment : Fragment() {

    private var binding: FragmentExploreBinding? = null

    private val exploreViewModel: ExploreViewModel by viewModels()

    private lateinit var adapter: PostGridListAdapter

    override fun onStart() {
        super.onStart()
        exploreViewModel.getPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentExploreBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

//        val gridLayoutManager = GridLayoutManager(context, 3)
//        adapter = PostGridListAdapter(mutableListOf())


        exploreViewModel.postsLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                SUCCESS -> {
                    displayProgressBar(false)
                    adapter = PostGridListAdapter(it.data!!)
                    binding?.recyclerView?.apply {
                        layoutManager = spannedGridLayoutManager
                        adapter = this@ExploreFragment.adapter
                    }

                }
                ERROR -> {
                    displayProgressBar(false)
                }
                LOADING -> {
                    displayProgressBar(true)
                }
                IDLE -> {

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