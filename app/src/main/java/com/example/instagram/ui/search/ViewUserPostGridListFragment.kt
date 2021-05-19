package com.example.instagram.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentViewUserPostGridListBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.view_post.PostGridListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewUserPostGridListFragment : Fragment() {
    companion object {
        private const val TAG = "ViewUserPostGridListFragment"

        fun newInstance(uid: String): ViewUserPostGridListFragment {
            return ViewUserPostGridListFragment().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                }
            }
        }
    }

    private var binding: FragmentViewUserPostGridListBinding? = null

    private val searchViewModel: SearchViewModel by activityViewModels()

    private var uid: String? = null

    private lateinit var postAdapter: PostGridListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString("uid")
        searchViewModel.getPostById(uid!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewUserPostGridListBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postAdapter = PostGridListAdapter(mutableListOf())
        postAdapter.onItemClicked = {
            val bundle = bundleOf("position" to it, "uid" to uid)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_otherUserFragment_to_otherUserPostsFragment, bundle)
        }
        binding?.photoRecyclerView?.apply {
            layoutManager = GridLayoutManager(view.context, 3)
            adapter = postAdapter
        }

        searchViewModel.otherUserPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    postAdapter.addAll(it.data!!.reversed())
                }
                ERROR -> {}
                LOADING -> {}
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}