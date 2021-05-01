package com.example.instagram.ui.explore.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.Status.*
import com.example.instagram.databinding.FragmentViewUserBinding
import com.example.instagram.ui.profile.ProfileViewPagerAdapter
import com.example.instagram.ui.profile.user_post.PostGridListFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/18/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ViewUserFragment : Fragment() {
    companion object {
        private const val TAG = "ViewUserFragment"
    }

    private var binding: FragmentViewUserBinding? = null

    private val searchViewModel: SearchViewModel by activityViewModels()

    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = arguments?.getString("uid")
        searchViewModel.getUserData(uid!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewUserBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val header = binding?.header!!
        val body = binding?.body!!

        val fragments = listOf(
            ViewUserPostGridListFragment.newInstance(uid!!),
            PostGridListFragment.newInstance(PostGridListFragment.TAGGED_PHOTOS_FRAGMENT)
        )
        body.viewPager2.adapter = ProfileViewPagerAdapter(this, fragments)
        TabLayoutMediator(body.tabLayout, body.viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_06_pixels)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_08_portrait)
                }
            }
        }.attach()

        searchViewModel.otherUserLiveData.observe(requireActivity(), {
            when (it.status) {
                SUCCESS -> {
                    val user = it.data!!
                    Glide.with(view.context)
                        .load(user.profile_photo)
                        .into(header.circleImageView)
                    header.displayNameTextView.text = user.display_name
                    header.bioTextView.text = user.bio
                    header.websiteTextView.text = user.website
                    header.postTextView.text = user.posts.toString()
                    header.followersTextView.text = user.followers.toString()
                    header.followingTextView.text = user.following.toString()
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}