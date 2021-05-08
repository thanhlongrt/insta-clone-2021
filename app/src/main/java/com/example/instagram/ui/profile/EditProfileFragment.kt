package com.example.instagram.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.instagram.*
import com.example.instagram.databinding.FragmentEditProfileBinding
import com.example.instagram.ui.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    companion object {
        const val TAG = "EditProfileFragment"
    }

    private var binding: FragmentEditProfileBinding? = null

    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val mainViewModel: MainViewModel by activityViewModels()

    private var isSaved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.currentUser.observe(requireActivity(), {
            when (it.status) {
                Status.SUCCESS -> {
                    val user = it.data!!
                    binding?.nameEditText?.setText(user.displayName)
                    binding?.usernameEditText?.setText(user.username)
                    binding?.websiteEditText?.setText(user.website)
                    binding?.bioEditText?.setText(user.bio)
                    if (!isSaved) {
                        activity?.let { it1 ->
                            Glide.with(it1)
                                .load(user.avatarUrl)
                                .into(binding?.circleImageView!!)
                        }
                    }
                }
            }
        })

        binding?.circleImageView?.setOnClickListener {
            takePhoto.launch(Unit)
        }

        binding?.textViewChangePhoto?.setOnClickListener {
            takePhoto.launch(Unit)
        }

        profileViewModel.uploadResult.observe(requireActivity(), {
            when (it.status) {
                Status.LOADING -> {
                    displayProgressBar(true)
                }
                Status.ERROR -> {
                    Snackbar.make(view, it.message!!, 5000).show()
                }

                Status.SUCCESS -> {
                    val userData = HashMap<String, Any>()
                    userData["profile_photo"] = it.data!!
                    profileViewModel.updateUserData(userData)
                    it.status = Status.IDLE
                }
                Status.IDLE -> {
                }
            }
        })

        profileViewModel.saveUserDataResult.observe(requireActivity(), {
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
                    if (isSaved) {
                        getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
                    }

                }
                Status.IDLE -> {
                }
            }
        })

    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        activity?.let {
            it.findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (isDisplayed) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_done, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
                val userData = HashMap<String, Any>()
                userData["display_name"] = binding?.nameEditText?.text.toString()
                userData["username"] = binding?.usernameEditText?.text.toString()
                userData["website"] = binding?.websiteEditText?.text.toString()
                userData["bio"] = binding?.bioEditText?.text.toString()
                profileViewModel.updateUserData(userData)
                isSaved = true
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private val takePhoto =
        registerForActivityResult(TakePhotoFromCameraOrGalleryContract()) { uri ->
            uri?.let {
                Log.e(TAG, "takePhoto: uri: $it ")
                Glide.with(requireActivity())
                    .load(uri)
                    .into(binding?.circleImageView!!)
                val path = "IMG_${System.currentTimeMillis()}.jpg"
                profileViewModel.upload(uri, path)
                isSaved = true
            }
        }

}