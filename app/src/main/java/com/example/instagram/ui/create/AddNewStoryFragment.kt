package com.example.instagram.ui.create

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.instagram.Constants.KEY_URI
import com.example.instagram.R
import com.example.instagram.databinding.FragmentAddNewStoryBinding
import com.example.instagram.getFragmentNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 5/4/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AddNewStoryFragment : Fragment() {

    companion object {
        private const val TAG = "AddNewStoryFragment"
    }

    private var binding: FragmentAddNewStoryBinding? = null

    private val createViewModel: CreateViewModel by activityViewModels()

    private var storyUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyUri = arguments?.getParcelable(KEY_URI)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddNewStoryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers(view)
    }

    private fun setupControllers(view: View) {
        storyUri?.let {
            Glide.with(view.context)
                .load(it)
                .into(binding?.imageView!!)
        }

        binding?.toolBar?.inflateMenu(R.menu.menu_done)
        binding?.toolBar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_done -> {
                    val user = createViewModel.currentUser.value
                    if (storyUri != null && user != null) {
                        val storagePath = "IMG_${System.currentTimeMillis()}.jpg"
                        val storyData = HashMap<String, Any>()
                        storyData["uid"] = user.uid
                        storyData["username"] = user.username
                        storyData["avatar_url"] = user.avatarUrl
                        storyData["video_url"] = ""
                        storyData["path"] = storagePath
                        storyData["date"] = System.currentTimeMillis()

                        createViewModel.saveStoryData(storyUri!!, storagePath, storyData)
                        getFragmentNavController(R.id.nav_host_fragment)?.navigate(
                            R.id.action_addNewStoryFragment_to_createBottomSheetFragment,
                        )
                    }
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}