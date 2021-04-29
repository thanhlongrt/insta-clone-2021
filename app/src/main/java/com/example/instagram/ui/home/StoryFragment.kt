package com.example.instagram.ui.home

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.instagram.ImageUtils
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentStoryBinding
import com.example.instagram.getFragmentNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Thanh Long Nguyen on 4/27/2021
 */

@AndroidEntryPoint
class StoryFragment : Fragment() {
    companion object {
        private const val TAG = "StoryFragment"
        private const val STORY_DURATION = 10 * 1000 // millisecond
    }

    private var binding: FragmentStoryBinding? = null

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var position: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt("position", 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val screenWidth = ImageUtils.getScreenWidth(requireActivity())

        homeViewModel.storiesLiveData.observe(requireActivity()) {
            when (it.status) {
                SUCCESS -> {
                    val userStory = it.data!![position!!]

                    for (story in userStory.stories) {
                        Glide.with(view.context)
                            .load(story.photo_url)
                            .preload()
                    }

                    var index = 0
                    val progressBarList = ArrayList<ProgressBar>()

                    val numberOfStories = userStory.stories.size
                    addProgressBars(screenWidth, numberOfStories, progressBarList)

                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            for (i in 0 until numberOfStories) {
                                Glide.with(view.context)
                                    .load(userStory.stories[i].photo_url)
                                    .into(binding?.imageView!!)
                                while (progressBarList[i].progress < STORY_DURATION) {
                                    delay(1)
                                    progressBarList[i].incrementProgressBy(1)
                                }

                                if (i == numberOfStories - 1 && progressBarList[i].progress == STORY_DURATION){
                                    getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
                                }

//                                this@withContext.launch {
//                                    val animator = ObjectAnimator.ofInt(
//                                        progressBarList[i],
//                                        "progress",
//                                        0,
//                                        STORY_DURATION*1000
//                                    )
//                                    animator.duration = (STORY_DURATION*1000).toLong()
//                                    animator.interpolator = LinearInterpolator()
//                                    animator.start()
//                                }
                            }
                        }

                    }


                }
                ERROR -> {

                }
                LOADING -> {

                }
                IDLE -> {

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addProgressBars(
        screenWidth: Int,
        numberOfStories: Int,
        progressBarList: ArrayList<ProgressBar>
    ) {
        val progressBarWidth =
            (screenWidth - (4 * (numberOfStories + 1))) / numberOfStories

        for (i in 0 until numberOfStories) {
            val progressBar = ProgressBar(
                context,
                null,
                android.R.attr.progressBarStyleHorizontal
            )
            progressBar.max = STORY_DURATION
            progressBar.isIndeterminate = false
            progressBar.progressTintList = ColorStateList.valueOf(
                resources.getColor(R.color.white, null)
            )
            progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.light_grey, null)
            )

            val params = LinearLayout.LayoutParams(progressBarWidth, 3)
            params.setMargins(4, 0, 0, 0)
            progressBar.layoutParams = params
            progressBarList.add(progressBar)
            binding?.progressBarHolder?.addView(progressBar)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}