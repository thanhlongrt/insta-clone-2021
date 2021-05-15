package com.example.instagram.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentViewUserPostListBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.model.PostItem
import com.example.instagram.network.entity.Notification
import com.example.instagram.ui.MainViewModel
import com.example.instagram.ui.profile.view_post.CacheDataSourceFactory
import com.example.instagram.ui.profile.view_post.PostListAdapter
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewUserPostListFragment : Fragment() {

    companion object {
        private const val TAG = "ViewUserPostsFragment"
    }

    private var binding: FragmentViewUserPostListBinding? = null

    private val searchViewModel: SearchViewModel by activityViewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var position: Int? = null

    private var uid: String? = null

    private lateinit var postListAdapter: PostListAdapter

    @Inject
    lateinit var cacheDataSourceFactory: CacheDataSourceFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            position = it.getInt("position", 0)
            uid = it.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewUserPostListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postListAdapter = PostListAdapter(mutableListOf())
        postListAdapter.apply {
            onLikeClick = { position, post ->
                if (!post.isLiked) {
                    sendLikePushNotification(post)
                }
                searchViewModel.clickLike(post.postId)
                postListAdapter.onLikeClick(position)
            }
            onCommentClick = { postId ->
                val bundle = bundleOf("postId" to postId)
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                    R.id.action_homeFragment_to_commentFragment,
                    bundle
                )
            }
            onViewAttachToWindow = { playerView, url ->
                if (player == null) {
                    this@ViewUserPostListFragment.initPlayer(playerView, url)
                }
            }

            onViewDetachedFromWindow = { playerView, url ->
                this@ViewUserPostListFragment.releasePlayer()
            }
        }

        val linearLayoutManager = LinearLayoutManager(view.context)

        binding?.userPostsRecyclerView?.apply {
            layoutManager = linearLayoutManager
            adapter = postListAdapter
            itemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            setHasFixedSize(true)
        }

        searchViewModel.otherUserPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    postListAdapter.addAll(it.data!!.reversed())
                    lifecycleScope.launch {
                        delay(100)
                        Log.e(TAG, "onViewCreated: Scroll with offset to $position")
                        linearLayoutManager.scrollToPositionWithOffset(position!!, 0)
                    }
                }
                ERROR -> {
                }
                LOADING -> {
                }
                IDLE -> {
                }
            }
        })
    }

    private fun sendLikePushNotification(post: PostItem) {
        lifecycleScope.launch {
            delay(3000)
            if (post.isLiked) {
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
                    mainViewModel.sendPushNotification(notification)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private var player: SimpleExoPlayer? = null
    private fun initPlayer(playerView: PlayerView, videoUrl: String) {
        val context = playerView.context
        player = SimpleExoPlayer.Builder(
            context,
        )
            .setLoadControl(DefaultLoadControl())
            .setTrackSelector(DefaultTrackSelector(context))
            .build()


        playerView.setKeepContentOnPlayerReset(true)
        playerView.useController = true
        playerView.player = this.player
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        val mediaItem =
            MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(MimeTypes.VIDEO_MP4)
                .build()

        val mediaSource = ProgressiveMediaSource.Factory(
            cacheDataSourceFactory
        ).createMediaSource(mediaItem)

        player?.apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE

            setMediaSource(mediaSource)
            prepare()
        }
    }

    private fun releasePlayer() {
        player?.let { player ->
            player.release()
            this.player = null
        }
    }

}