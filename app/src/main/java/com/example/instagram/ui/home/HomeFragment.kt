package com.example.instagram.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.ProfileViewModel
import com.example.instagram.ui.profile.user_post.PostListAdapter
import com.example.instagram.ui.profile.user_post.PostListFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var postListAdapter: PostListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.getStoryData()
        homeViewModel.getAllPosts()
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

        val storyAdapter = StoryListAdapter(mutableListOf())
        storyAdapter.onItemClick = { position ->
            val bundle = bundleOf("position" to position)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_homeFragment_to_storyFragment,
                bundle
            )
        }
        binding?.storyRecyclerView?.apply {
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
            }
        }

        postListAdapter =
            PostListAdapter(mutableListOf())
        postListAdapter.onLikeClick = { position, post ->
            val uid = homeViewModel.currentUserUid
            if (post.isLiked) {
                postListAdapter.unlike(position, uid)
                homeViewModel.getLikeId(uid, post.postId)
            } else {
                val likeData = HashMap<String, Any>()
                likeData["uid"] = uid
                likeData["like_id"] = ""
                likeData["post_id"] = post.postId
                likeData["comment_id"] = ""
                homeViewModel.like(likeData)
                postListAdapter.like(position, uid)
            }
        }

        linearLayoutManager = LinearLayoutManager(view.context)

        binding?.postRecyclerView?.apply {

            this.layoutManager = linearLayoutManager
            adapter = postListAdapter
            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            setHasFixedSize(true)
        }

        homeViewModel.userPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    displayProgressBar(false)
                    postListAdapter.addAll(it.data!!.reversed())
                }
                ERROR -> {
                    displayProgressBar(false)
                }
                LOADING -> {
                    displayProgressBar(true)
                }
                IDLE -> TODO()
            }
        })

        homeViewModel.likeIdToDelete.observe(requireActivity()) {
            when (it.status) {
                SUCCESS -> {
                    homeViewModel.unlike(it.data!!)
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