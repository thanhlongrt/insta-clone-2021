package com.example.instagram.ui.profile.user_post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentPostGridListBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.create_new.PostViewModel

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
class PostGridListFragment : Fragment() {
    private var binding: FragmentPostGridListBinding? = null

    private val postViewModel: PostViewModel by activityViewModels()

    private lateinit var postAdapter: PostGridListAdapter

    private var type: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            type = it.getInt("type")
        }

        postViewModel.getPosts()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostGridListBinding.inflate(inflater, container, false)

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postAdapter = PostGridListAdapter(mutableListOf())
        postAdapter.onItemClicked = {
            val bundle = bundleOf("post_position" to it)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_profileFragment_to_userPostsFragment,
                bundle
            )
        }
        when (type) {
            UPLOADED_PHOTOS_FRAGMENT -> {
                binding?.photoRecyclerView?.apply {
                    setHasFixedSize(true)
                    adapter = postAdapter
                    val layoutManager = GridLayoutManager(view.context, 3)
                    this.layoutManager = layoutManager

                }
            }
            TAGGED_PHOTOS_FRAGMENT -> {

            }
        }

        postViewModel.postLiveData.observe(requireActivity(), {
            when (it.status) {
                Status.LOADING -> {
                    displayProgressBar(true)
                }

                Status.ERROR -> {
                    displayProgressBar(false)
                }

                Status.SUCCESS -> {
                    displayProgressBar(false)
                    postAdapter.addAll(it.data!!.reversed())
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

    companion object {
        const val UPLOADED_PHOTOS_FRAGMENT = 1
        const val TAGGED_PHOTOS_FRAGMENT = 2

        fun newInstance(type: Int) =
            PostGridListFragment().apply {
                arguments = Bundle().apply {
                    putInt("type", type)
                }
            }
    }

}