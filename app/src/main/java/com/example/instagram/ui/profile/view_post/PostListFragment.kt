package com.example.instagram.ui.profile.view_post

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
import com.example.instagram.Status.SUCCESS
import com.example.instagram.databinding.FragmentPostListBinding
import com.example.instagram.extensions.getFragmentNavController
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
 * Created by Thanh Long Nguyen on 4/16/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PostListFragment : Fragment() {

    companion object {
        private const val TAG = "PostListFragment"
    }

    private var binding: FragmentPostListBinding? = null

    private val viewPostViewModel: ViewPostViewModel by activityViewModels()

    private lateinit var postListAdapter: PostListAdapter

    private lateinit var linearLayoutManager: LinearLayoutManager

    private var position: Int? = null

    @Inject
    lateinit var cacheDataSourceFactory: CacheDataSourceFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("post_position")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding!!.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.backButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        postListAdapter = PostListAdapter(mutableListOf())
        postListAdapter.apply {
            onOptionClick = {
                val bundle = bundleOf(
                    "path" to it.path,
                    "post_id" to it.postId
                )
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                    R.id.action_userPostsFragment_to_postBottomSheetFragment,
                    bundle
                )
            }

            onLikeClick = { position, post ->
                viewPostViewModel.like(post.postId)
                onLikeClick(position)
            }
            onCommentClick = { postId ->
                val bundle = bundleOf("postId" to postId)
                getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                    R.id.action_userPostsFragment_to_commentFragment2,
                    bundle
                )
            }

            onViewAttachToWindow = { playerView, url ->
                if (player == null) {
                    this@PostListFragment.initPlayer(playerView, url)
                }
            }

            onViewDetachedFromWindow = { playerView, url ->
                this@PostListFragment.releasePlayer()
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

        viewPostViewModel.userPosts.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    postListAdapter.addAll(it.data!!.reversed())

                    lifecycleScope.launch {
                        delay(50)
                        Log.e(TAG, "onViewCreated: Scroll with offset to $position")
                        linearLayoutManager.scrollToPositionWithOffset(position!!, 0)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private var player: SimpleExoPlayer? = null
    fun initPlayer(playerView: PlayerView, videoUrl: String) {
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