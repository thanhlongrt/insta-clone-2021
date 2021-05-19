package com.example.instagram.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.instagram.R
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.ui.login.LoginActivity
import com.example.instagram.ui.profile.view_post.PostGridListFragment
import com.example.instagram.ui.profile.view_post.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ProfileFragment : Fragment() {
    companion object {
        const val TAG = "ProfileFragment"
    }

    private var binding: FragmentProfileBinding? = null

    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding?.viewModel = profileViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        profileViewModel.currentUser.observe(requireActivity()) { user ->
//            user?.let {
//                requireActivity().findViewById<Toolbar>(R.id.tool_bar).title = it.username
//            }
//        }

        setupControllers()

    }

    private fun setupControllers() {

        binding?.toolBar?.inflateMenu(R.menu.menu_profile)
        binding?.toolBar?.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_logout -> {
                    logout()
                    true
                }

                R.id.action_add -> {
                    findNavController().navigate(R.id.action_profileFragment_to_nav_create)
                    true
                }

                else -> {
                    super.onOptionsItemSelected(menuItem)
                }
            }
        }

        binding?.editProfileButton?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        val fragments = listOf(
            PostGridListFragment.newInstance(PostGridListFragment.UPLOADED_POSTS_FRAGMENT),
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }

            R.id.action_add -> {
                findNavController().navigate(R.id.action_profileFragment_to_nav_create)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


    private fun logout() {
        profileViewModel.logout()
        activity?.let {
            val intent = Intent(it, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}