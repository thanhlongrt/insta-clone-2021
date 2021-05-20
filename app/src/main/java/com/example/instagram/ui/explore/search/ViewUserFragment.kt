package com.example.instagram.ui.explore.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.instagram.R
import com.example.instagram.databinding.FragmentViewUserBinding
import com.example.instagram.extensions.getFragmentNavController
import com.example.instagram.ui.profile.view_post.PostGridListFragment
import com.example.instagram.ui.profile.view_post.ViewPagerAdapter
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
        searchViewModel.getUserDataById(uid!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_user, container, false)
        binding?.viewmodel = searchViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers()

    }

    private fun setupControllers() {
        binding?.backButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        val fragments = listOf(
            ViewUserPostGridListFragment.newInstance(uid!!),
            PostGridListFragment.newInstance(PostGridListFragment.TAGGED_POSTS_FRAGMENT),
        )
        binding?.viewPager2?.adapter = ViewPagerAdapter(this, fragments)
        TabLayoutMediator(binding?.tabLayout!!, binding?.viewPager2!!) { tab, position ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_06_pixels)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_08_portrait)
                }
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}