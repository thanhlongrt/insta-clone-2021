package com.example.instagram.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentViewUserPostListBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.PostListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewUserPostListFragment : Fragment() {

    companion object {
        private const val TAG = "ViewUserPostsFragment"
    }

    private var binding: FragmentViewUserPostListBinding? = null

    private val searchViewModel: SearchViewModel by activityViewModels()

    private var position: Int? = null

    private var uid: String? = null

    private lateinit var postListAdapter: PostListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            position = it.getInt("position", 0)
            uid = it.getString("uid")!!
        }
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
            PostListAdapter(mutableListOf())
        postListAdapter = PostListAdapter(mutableListOf())

        postListAdapter.onLikeClick = { position, post ->
            searchViewModel.clickLike(post.postId)
            postListAdapter.clickLike(position)
        }

        postListAdapter.onCommentClick = { postId ->
            val bundle = bundleOf("postId" to postId)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_homeFragment_to_commentFragment,
                bundle
            )
        }

        val linearLayoutManager = LinearLayoutManager(view.context)

        binding?.userPostsRecyclerView?.apply {
            layoutManager = linearLayoutManager
            adapter = postListAdapter
            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            setHasFixedSize(true)
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