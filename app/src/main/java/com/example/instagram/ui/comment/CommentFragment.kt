package com.example.instagram.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Constants.KEY_POST_JSON
import com.example.instagram.R
import com.example.instagram.TypeConverters
import com.example.instagram.databinding.FragmentCommentBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.model.PostItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 5/4/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CommentFragment : Fragment() {

    companion object {
        private const val TAG = "CommentFragment"
    }

    private var binding: FragmentCommentBinding? = null

    private val commentViewModel: CommentViewModel by viewModels()

    private var postItem: PostItem? = null

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(KEY_POST_JSON)?.let {
            postItem = TypeConverters.jsonToPostItem(it)
            commentViewModel._currentPost.value = postItem
            commentViewModel.getCommentsByPost(postItem!!.postId)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment, container, false)
        binding?.viewModel = commentViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers(view)

        configObservers()

    }

    private fun configObservers() {
        commentViewModel.comments.observe(viewLifecycleOwner) {
            commentAdapter.addAll(it.reversed())
        }
    }

    private fun setupControllers(view: View) {
        binding?.backButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        commentAdapter = CommentAdapter(mutableListOf())


        binding?.commentRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = commentAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}