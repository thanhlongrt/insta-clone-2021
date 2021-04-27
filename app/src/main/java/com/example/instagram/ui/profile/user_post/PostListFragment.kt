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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentPostListBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.ProfileViewModel
import com.example.instagram.ui.profile.create_new.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/16/2021
 */

@AndroidEntryPoint
class PostListFragment : Fragment() {

    companion object {
        private const val TAG = "PostListFragment"
    }

    private var binding: FragmentPostListBinding? = null

    private val postViewModel: PostViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
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

        postListAdapter = PostListAdapter(profileViewModel.userLiveData.value!!.data!!, mutableListOf())
        postListAdapter.onOptionClick = {
            val bundle = bundleOf(
                "photo_path" to it.path,
                "photo_id" to it.post_id
            )
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_userPostsFragment_to_postBottomSheetFragment,
                bundle
            )
        }

        linearLayoutManager = LinearLayoutManager(view.context)

        binding?.userPostsRecyclerView?.apply {

            this.layoutManager = linearLayoutManager
            adapter = postListAdapter
        }

        Log.e(TAG, "onViewCreated: ")

        postViewModel.postLiveData.observe(requireActivity(), {
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

        postViewModel.deletePostResult.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    postViewModel.getPosts()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}