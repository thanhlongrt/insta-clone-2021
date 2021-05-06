package com.example.instagram.ui.create

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.instagram.ui.MainViewModel
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentCreateNewPostBinding
import com.example.instagram.getFragmentNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CreateNewPostFragment : Fragment() {

    companion object {
        private const val TAG = "CreateNewPostFragment"
    }

    private var binding: FragmentCreateNewPostBinding? = null

    private val createViewModel: CreateViewModel by activityViewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var photoUri: Uri? = null
    private var isDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        photoUri = arguments?.getParcelable("uri")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateNewPostBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = arguments?.getParcelable<Uri>("uri")

        mainViewModel.userLiveData.observe(requireActivity(), {
            when (it.status) {
                Status.SUCCESS -> {
                    val user = it.data!!
                    binding?.avatar?.let { imageView ->
                        Glide.with(view.context)
                            .load(user.avatarUrl)
                            .into(imageView)
                    }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
                Status.IDLE -> {

                }
            }
        })

        binding?.imageView?.let {
            Glide.with(view.context)
                .load(uri)
                .into(it)
        }
    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        activity?.let {
            it.findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (isDisplayed) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
//                photoUri?.let { photoViewModel.upload(it, PhotoViewModel.ImageType.Photo) }
                photoUri?.let { uri ->
                    val storagePath = "IMG_${System.currentTimeMillis()}.jpg"
                    val photoData = HashMap<String, Any>()
                    photoData["uid"] = mainViewModel.userLiveData.value!!.data!!.uid
                    photoData["avatar_url"] = mainViewModel.userLiveData.value!!.data!!.avatarUrl
                    photoData["user_name"] = mainViewModel.userLiveData.value!!.data!!.username
                    photoData["date_created"] = System.currentTimeMillis()
                    photoData["caption"] = binding?.captionEditText?.text.toString()
                    photoData["path"] = storagePath
                    photoData["like_count"] = 0
                    photoData["comment_count"] = 0
                    createViewModel.savePostData(uri, storagePath, photoData)

                    getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_createNewPostFragment_to_profileFragment)
                }
                isDone = true
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