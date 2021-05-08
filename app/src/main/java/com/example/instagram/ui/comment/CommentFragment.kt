package com.example.instagram.ui.comment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentCommentBinding
import com.example.instagram.model.UserItem
import com.example.instagram.network.entity.Notification
import com.example.instagram.ui.MainViewModel
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

    private val mainViewModel: MainViewModel by activityViewModels()

    private val commentViewModel: CommentViewModel by viewModels()

    private val userData: UserItem by lazy { mainViewModel.currentUser.value!!.data!! }

    private var postId: String? = null

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString("postId")
        postId?.let { commentViewModel.getCommentsByPost(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(view.context)
            .load(mainViewModel.currentUser.value!!.data!!.avatarUrl)
            .into(binding?.avatar!!)

        commentAdapter = CommentAdapter(mutableListOf())
        binding?.commentRecyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = commentAdapter
        }

        commentViewModel.comments.observe(viewLifecycleOwner) {
            if (it.status == Status.SUCCESS) {
                commentAdapter.addAll(it.data!!.reversed())
            }
        }

        binding?.commentButton?.setOnClickListener {
            val content = binding?.commentEditText?.text.toString()
            if (!TextUtils.isEmpty(content) && postId != null) {
                val commentData = HashMap<String, Any>()
                commentData["uid"] = userData.uid
                commentData["avatar"] = userData.avatarUrl
                commentData["username"] = userData.username
                commentData["content"] = content
                commentData["post_id"] = postId!!
                commentData["date_created"] = System.currentTimeMillis()

                commentViewModel.addComment(commentData)

//                val notification = Notification(
//                    uid = post.uid,
//                    post_id = post.postId,
//                    title = "Instagram",
//                    body = "${post.userName}: ${it.username} liked your post",
//                    date = System.currentTimeMillis(),
//                    sender_avatar = it.avatarUrl,
//                    seen = false
//                )
//                commentViewModel.sendPushNotification()

                binding?.commentEditText?.setText("")
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}