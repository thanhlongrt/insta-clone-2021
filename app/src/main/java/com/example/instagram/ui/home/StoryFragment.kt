package com.example.instagram.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.navGraphViewModels
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.FragmentStoryBinding
import com.example.instagram.extensions.getFragmentNavController
import com.example.instagram.model.UserStoryItem
import com.example.instagram.extensions.setDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/27/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class StoryFragment : Fragment(), StoryProgressView.StoriesListener {
    companion object {
        private const val TAG = "StoryFragment"
        private const val STORY_DURATION = 10 * 1000 // millisecond
    }

    private var binding: FragmentStoryBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var position: Int? = null

    private val userStoryItem: UserStoryItem by lazy { homeViewModel.stories.value!!.data!![position!!] }

    private var storyIndex: Int = 0

    private var pressTime = 0L

    private var limit = 500L

    private val onTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.e(TAG, "onTouch: Action Down")
                    pressTime = System.currentTimeMillis()
                    binding?.progressBarContainer?.pause()
                    return false
                }
                MotionEvent.ACTION_UP -> {
                    Log.e(TAG, "onTouch: Action Up")
                    binding?.progressBarContainer?.resume()
                    return limit < System.currentTimeMillis() - pressTime
                }
            }
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position", 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers(view)

    }

    private fun setupControllers(view: View) {
        binding?.apply {
            progressBarContainer.apply {
                setStoriesCount(userStoryItem.stories.size)
                setStoryDuration(10000L)
                setStoriesListener(this@StoryFragment)
            }
            Glide.with(view.context)
                .load(userStoryItem.stories[storyIndex].photoUrl)
                .into(imageView)
            Glide.with(view.context)
                .load(userStoryItem.avatarUrl)
                .into(avatar)
            username.text = userStoryItem.username
            date.setDate(userStoryItem.stories[storyIndex].date)
            progressBarContainer.startStories(storyIndex)

            reverse.setOnClickListener {
                progressBarContainer.reverse()
            }

            skip.setOnClickListener {
                progressBarContainer.skip()
            }

            reverse.setOnTouchListener(onTouchListener)
            skip.setOnTouchListener(onTouchListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onNext() {
        context?.let {
            ++storyIndex
            binding?.apply {
                date.setDate(userStoryItem.stories[storyIndex].date)
                Glide.with(it)
                    .load(userStoryItem.stories[storyIndex].photoUrl)
                    .into(imageView)
            }
        }
    }

    override fun onPrev() {
        if (storyIndex - 1 < 0) return
        context?.let {
            --storyIndex
            binding?.apply {
                date.setDate(userStoryItem.stories[storyIndex].date)
                Glide.with(it)
                    .load(userStoryItem.stories[storyIndex].photoUrl)
                    .into(imageView)
            }
        }
    }

    override fun onComplete() {
        getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
    }

    override fun onDestroy() {
        binding?.progressBarContainer?.destroy()
        super.onDestroy()
    }
}