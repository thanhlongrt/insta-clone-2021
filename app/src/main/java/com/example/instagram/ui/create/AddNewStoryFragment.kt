package com.example.instagram.ui.create

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.databinding.FragmentAddNewStoryBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 5/4/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddNewStoryFragment : Fragment() {

    private var binding: FragmentAddNewStoryBinding? = null

    private val createViewModel: CreateViewModel by activityViewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var storyUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyUri = arguments?.getParcelable("story_uri")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNewStoryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storyUri?.let {
            Glide.with(view.context)
                .load(it)
                .into(binding?.imageView!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
                storyUri?.let {
                    val storagePath = "IMG_${System.currentTimeMillis()}.jpg"
                    val storyData = HashMap<String, Any>()
                    storyData["uid"] = mainViewModel.currentUser.value!!.data!!.uid
                    storyData["username"] = mainViewModel.currentUser.value!!.data!!.username
                    storyData["avatar_url"] = mainViewModel.currentUser.value!!.data!!.avatarUrl
                    storyData["video_url"] = ""
                    storyData["path"] = storagePath

                    createViewModel.saveStoryData(it, storagePath, storyData)

                    getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_addNewStoryFragment_to_profileFragment)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}