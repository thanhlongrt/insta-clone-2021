package com.example.instagram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.Status.SUCCESS
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.model.PostItem
import com.example.instagram.network.entity.Notification
import com.example.instagram.ui.MainViewModel
import com.example.instagram.ui.profile.PostListAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
@ExperimentalCoroutinesApi
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var postListAdapter: PostListAdapter

    private lateinit var storyListAdapter: StoryListAdapter

    private lateinit var concatAdapter: ConcatAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

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

        postListAdapter = PostListAdapter(mutableListOf())

        postListAdapter.onLikeClick = { position, post ->
            if (!post.isLiked && post.uid!= mainViewModel.currentUser.value?.data?.uid ?: false) {
                sendLikePushNotification(post)
            }
            homeViewModel.clickLike(post.postId)
            postListAdapter.clickLike(position)
        }

        postListAdapter.onCommentClick = { postId ->
            val bundle = bundleOf("postId" to postId)
            getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                R.id.action_homeFragment_to_commentFragment,
                bundle
            )
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
            if (it.status == SUCCESS) {
                storyListAdapter.addAll(it.data!!)
            }
        }

        homeViewModel.feedPosts.observe(requireActivity(), {
            if (it.status == SUCCESS) {
                postListAdapter.addAll(it.data!!.reversed())
            }
        })

    }

    private fun sendLikePushNotification(post: PostItem) {
        lifecycleScope.launch {
            delay(3000)
            if (!post.isLiked) {
                mainViewModel.currentUser.value?.data?.let {
                    val notification = Notification(
                        uid = post.uid,
                        post_id = post.postId,
                        title = "Instagram",
                        body = "${post.userName}: ${it.username} liked your post",
                        date = System.currentTimeMillis(),
                        sender_avatar = it.avatarUrl,
                        seen = false
                    )
                    homeViewModel.sendPushNotification(notification)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}