package com.example.instagram.ui.profile.user_post

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
import com.example.instagram.databinding.FragmentPostListBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.ProfileViewModel
import com.example.instagram.ui.profile.create_new.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostListFragment : Fragment() {

    companion object {
        private const val TAG = "PostListFragment"
    }

    private var binding: FragmentPostListBinding? = null

    private val postViewModel: PostViewModel by activityViewModels()
    private lateinit var postListAdapter: PostListAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var position: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("post_position")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postListAdapter =
            PostListAdapter(mutableListOf())
        postListAdapter.onOptionClick = {
            val bundle = bundleOf(
                "photo_path" to it.path,
                "photo_id" to it.postId
            )
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_userPostsFragment_to_postBottomSheetFragment,
                bundle
            )
        }
        postListAdapter.onLikeClick = { position, post ->
            val uid = postViewModel.currentUserUid
            if (post.isLiked) {
                postListAdapter.unlike(position, uid)
                postViewModel.getLikeId(uid, post.postId)
            } else {
                val likeData = HashMap<String, Any>()
                likeData["uid"] = uid
                likeData["like_id"] = ""
                likeData["post_id"] = post.postId
                likeData["comment_id"] = ""
                postViewModel.like(likeData)
                postListAdapter.like(position, uid)
            }
        }

        linearLayoutManager = LinearLayoutManager(view.context)

        binding?.userPostsRecyclerView?.apply {

            this.layoutManager = linearLayoutManager
            adapter = postListAdapter
            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            setHasFixedSize(true)
        }

        postViewModel.userPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    displayProgressBar(false)
                    postListAdapter.addAll(it.data!!.reversed())

                    lifecycleScope.launch {
                        delay(100)
                        Log.e(TAG, "onViewCreated: Scroll with offset to $position")
                        linearLayoutManager.scrollToPositionWithOffset(position!!, 0)
                    }

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

        postViewModel.likeIdToDelete.observe(requireActivity()) {
            when(it.status){
                SUCCESS -> {
                    postViewModel.unlike(it.data!!)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}