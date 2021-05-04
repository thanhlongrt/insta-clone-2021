package com.example.instagram.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentProfileBinding
import com.example.instagram.ui.MainViewModel
import com.example.instagram.ui.login.LoginActivity
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

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        profileViewModel.getPostById(profileViewModel.currentUserUid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        configObservers(view)

        setupControllers()


    }

    private fun setupControllers() {
        binding?.editProfileButton?.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }


        val fragments = listOf(
            PostGridListFragment.newInstance(PostGridListFragment.UPLOADED_PHOTOS_FRAGMENT),
            PostGridListFragment.newInstance(PostGridListFragment.TAGGED_PHOTOS_FRAGMENT),
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

    private fun configObservers(view: View) {
//        profileViewModel.saveUserDataResult.observe(requireActivity(), {
//            when (it.status) {
//                Status.SUCCESS -> {
//                    mainViewModel.getCurrentUser()
//                }
//            }
//        })

        mainViewModel.userLiveData.observe(requireActivity(), {
            when (it.status) {
                Status.SUCCESS -> {
                    val user = it.data!!
                    if (user.avatarUrl != "") {
                        binding?.circleImageView?.let { it1 ->
                            Glide.with(view.context)
                                .load(user.avatarUrl)
                                .into(it1)
                        }
                    }
                    binding?.displayNameTextView?.text = user.displayName
                    binding?.bioTextView?.text = user.bio
                    binding?.websiteTextView?.text = user.website
                }
            }
        })

        profileViewModel.userPosts.observe(requireActivity()) {
            when(it.status){
                Status.SUCCESS -> {
                    binding?.postCount?.text = it.data!!.size.toString()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }

            R.id.action_add -> {

                findNavController().navigate(R.id.action_profileFragment_to_createBottomSheetFragment)
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