package com.example.instagram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.Status.SUCCESS
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.PostListAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
@ExperimentalCoroutinesApi
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var postListAdapter: PostListAdapter

    private lateinit var storyListAdapter: StoryListAdapter

    private lateinit var concatAdapter: ConcatAdapter

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

        storyListAdapter = StoryListAdapter(mutableListOf())
        storyListAdapter.onItemClick = { position ->
            val bundle = bundleOf("position" to position)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_homeFragment_to_storyFragment,
                bundle
            )
        }
//        binding?.storyRecyclerView?.apply {
//            layoutManager = LinearLayoutManager(
//                view.context,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//            adapter = storyAdapter
//        }


        postListAdapter =
            PostListAdapter(mutableListOf())

//        postListAdapter.onStoryClick = { position ->
//            val bundle = bundleOf("position" to position)
//            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
//                R.id.action_homeFragment_to_storyFragment,
//                bundle
//            )
//        }
        postListAdapter.onLikeClick = { position, post ->
            val uid = homeViewModel.currentUserUid
            if (post.isLiked) {
                postListAdapter.unlike(position, uid)
                homeViewModel.unlike(uid, post.postId)
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

        concatAdapter = ConcatAdapter(HorizontalAdapter(storyListAdapter), postListAdapter)

        linearLayoutManager = LinearLayoutManager(view.context)

        binding?.postRecyclerView?.apply {

            this.layoutManager = linearLayoutManager
            adapter = concatAdapter
            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
//            setHasFixedSize(true)
        }

        homeViewModel.stories.observe(requireActivity()) {
            when (it.status) {
                SUCCESS -> {
                    storyListAdapter.addAll(it.data!!)
                }
            }
        }

        homeViewModel.feedPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    displayProgressBar(false)
                    postListAdapter.addAll(it.data!!.reversed())
                }
            }
        })

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