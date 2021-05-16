package com.example.instagram.ui.comment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.databinding.FragmentCommentBinding
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

    private var postId: String? = null

    private lateinit var commentAdapter: CommentAdapter

    private lateinit var commentHeaderAdapter: CommentHeaderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString("postId")
        postId?.let {
            commentViewModel.getPostById(it)
            commentViewModel.getCommentsByPost(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comment, container, false)
        binding?.viewModel = commentViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commentHeaderAdapter = CommentHeaderAdapter(null)
        commentAdapter = CommentAdapter(mutableListOf())


        val concatAdapter = ConcatAdapter(commentHeaderAdapter, commentAdapter)
        binding?.commentRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = concatAdapter
        }

        commentViewModel.comments.observe(viewLifecycleOwner) {
            commentAdapter.addAll(it.reversed())
        }

        commentViewModel.currentPost.observe(viewLifecycleOwner) {
            Log.e(TAG, "onViewCreated: ${it.userName}")
            commentHeaderAdapter.setPost(it)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}