package com.example.instagram.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentEditProfileBinding
import com.example.instagram.extensions.getFragmentNavController
import com.example.instagram.ui.create.TakePhotoContract
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

    private var isSaved: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        binding?.viewModel = profileViewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers()

        configObservers(view)

    }

    private fun configObservers(view: View) {
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
            }
        })
    }

    private fun setupControllers() {
        binding?.toolBar?.inflateMenu(R.menu.menu_done)
        binding?.toolBar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
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

        binding?.cancelButton?.setOnClickListener {
            getFragmentNavController(R.id.nav_host_fragment)?.navigateUp()
        }

        binding?.circleImageView?.setOnClickListener {
            takePhoto.launch(Unit)
        }

        binding?.textViewChangePhoto?.setOnClickListener {
            takePhoto.launch(Unit)
        }
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

    private val takePhoto =
        registerForActivityResult(TakePhotoContract()) { uri ->
            uri?.let {
                Log.e(TAG, "takePhoto: uri: $it ")
                Glide.with(requireActivity())
                    .load(uri)
                    .into(binding?.circleImageView!!)
                val path = "IMG_${System.currentTimeMillis()}.jpg"
                profileViewModel.uploadProfilePicture(uri, path)
                isSaved = true
            }
        }

}