package com.example.instagram.ui.explore.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentViewUserPostListBinding
import com.example.instagram.ui.profile.user_post.PostListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */

@AndroidEntryPoint
class ViewUserPostListFragment : Fragment() {

    companion object {
        private const val TAG = "ViewUserPostListFragment"
    }

    private var binding: FragmentViewUserPostListBinding? = null

    private val searchViewModel: SearchViewModel by activityViewModels()

    private var position: Int? = null

    private lateinit var postListAdapter: PostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewUserPostListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postListAdapter =
            PostListAdapter(searchViewModel.otherUserLiveData.value!!.data!!, mutableListOf())

        val linearLayoutManager = LinearLayoutManager(view.context)

        binding?.userPostsRecyclerView?.apply {
            layoutManager = linearLayoutManager
            adapter = postListAdapter
        }

        searchViewModel.otherUserPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    postListAdapter.addAll(it.data!!.reversed())
                    lifecycleScope.launch {
                        delay(100)
                        Log.e(TAG, "onViewCreated: Scroll with offset to $position")
                        linearLayoutManager.scrollToPositionWithOffset(position!!, 0)
                    }
                }
                ERROR -> {
                }
                LOADING -> {
                }
                IDLE -> {
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}