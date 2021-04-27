package com.example.instagram.ui.profile.create_new

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentCreateNewPostBinding
import com.example.instagram.getFragmentNavController
import com.example.instagram.ui.profile.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Thanh Long Nguyen on 4/14/2021
 */

@AndroidEntryPoint
class CreateNewPostFragment : Fragment() {

    companion object {
        private const val TAG = "CreateNewPostFragment"
    }

    private var binding: FragmentCreateNewPostBinding? = null

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val postViewModel: PostViewModel by viewModels()

    private var photoUri: Uri? = null
    private var isDone = false

    private lateinit var photoStoragePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        photoUri = arguments?.getParcelable<Uri>("uri")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateNewPostBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = arguments?.getParcelable<Uri>("uri")

        profileViewModel.userLiveData.observe(requireActivity(), {
            when (it.status) {
                Status.SUCCESS -> {
                    val user = it.data!!
                    binding?.profileImage?.let { imageView ->
                        Glide.with(view.context)
                            .load(user.profile_photo)
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

        postViewModel.uploadResult.observe(viewLifecycleOwner, {
            when (it.status) {
                Status.LOADING -> {
                    displayProgressBar(true)
                }
                Status.ERROR -> {
                    Snackbar.make(view, it.message!!, 5000).show()
                }
                Status.SUCCESS -> {
                    val photoData = HashMap<String, Any>()
                    photoData["uid"] = profileViewModel.userLiveData.value!!.data!!.uid
                    photoData["url"] = it.data!!
                    photoData["date_created"] = System.currentTimeMillis()
                    photoData["caption"] = binding?.captionEditText?.text.toString()
                    photoData["tags"] = 0
                    photoData["likes"] = 0
                    photoData["comments"] = 0
                    photoData["path"] = photoStoragePath
                    postViewModel.savePostData(photoData)
                }
                Status.IDLE -> {

                }
            }
        })

        postViewModel.savePostDataResult.observe(requireActivity(), {
            when (it.status) {
                Status.LOADING -> {
                    displayProgressBar(true)
                }
                Status.ERROR -> {
                    displayProgressBar(false)
                    Snackbar.make(view, it.message!!, 5000).show()
                }
                Status.SUCCESS -> {
                    displayProgressBar(false)
                    if (isDone) {
                        getFragmentNavController(R.id.nav_host_fragment)?.navigate(R.id.action_createNewPostFragment_to_profileFragment)
                    }
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
        inflater.inflate(R.menu.menu_edit_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
//                photoUri?.let { photoViewModel.upload(it, PhotoViewModel.ImageType.Photo) }

                photoUri?.let {
                    photoStoragePath = "IMG_${System.currentTimeMillis()}.jpg"
                    postViewModel.upload(it, photoStoragePath)
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