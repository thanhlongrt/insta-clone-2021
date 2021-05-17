package com.example.instagram.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Constants.KEY_POST_JSON
import com.example.instagram.R
import com.example.instagram.Status.SUCCESS
import com.example.instagram.TypeConverters
import com.example.instagram.databinding.FragmentHomeBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.model.PostItem
import com.example.instagram.network.entity.Notification
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
 * Created by Thanh Long Nguyen on 4/12/2021
 */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment() {
    companion object {
        private const val TAG = "HomeFragment"
    }

    private var binding: FragmentHomeBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private lateinit var postListAdapter: PostListAdapter

    private lateinit var storyListAdapter: StoryListAdapter

    private lateinit var concatAdapter: ConcatAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    @Inject
    lateinit var cacheDataSourceFactory: CacheDataSourceFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

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
        postListAdapter.apply {
            onLikeClick = { position, post ->
                if (!post.isLiked && post.uid != homeViewModel.currentUser.value?.uid ?: false) {
                    sendLikePushNotification(post)
                }
                homeViewModel.clickLike(post.postId)
                onLikeClick(position)
            }

            onCommentClick = { postItem ->
                val bundle = bundleOf(KEY_POST_JSON to TypeConverters.postItemToJson(postItem))
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                    R.id.action_homeFragment_to_commentFragment,
                    bundle
                )
            }

            onViewAttachToWindow = { playerView, url ->
                if (player == null) {
                    this@HomeFragment.initPlayer(playerView, url)
                }
            }

            onViewDetachedFromWindow = { _, _ ->
                this@HomeFragment.releasePlayer()
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
            if (post.isLiked) {
                homeViewModel.currentUser.value?.let {
                    Log.e(TAG, "sendLikePushNotification: ...")
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_homeFragment_to_createBottomSheetFragment2)
                true
            }

            R.id.action_direct -> {
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_homeFragment_to_directFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}