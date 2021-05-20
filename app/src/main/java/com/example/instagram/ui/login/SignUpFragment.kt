package com.example.instagram.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentSignUpBinding
import com.example.instagram.extensions.afterTextChanged
import com.example.instagram.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_sign_up, container, false)
        binding?.loginViewModel = loginViewModel
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupControllers()

        configObservers(view)
    }

    private fun setupControllers() {
        binding?.email?.apply {
            afterTextChanged {
                loginViewModel.signUpDataChanged()
            }
        }

        binding?.displayName?.apply {
            afterTextChanged {
                loginViewModel.signUpDataChanged()
            }
        }

        binding?.password?.apply {
            afterTextChanged {
                loginViewModel.signUpDataChanged()
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.signUp()
                }
                false
            }
        }
    }

    private fun configObservers(view: View) {
        loginViewModel.signUpFormState.observe(viewLifecycleOwner, Observer {
            val formState = it ?: return@Observer

            binding?.signUp?.isEnabled = formState.data!!
        })

        loginViewModel.signUpResult.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    displayProgressBar(true)
                }

                Status.ERROR -> {
                    displayProgressBar(false)
                    Snackbar.make(view, it.message.toString(), 5000).show()
                }

                Status.SUCCESS -> {
                    displayProgressBar(false)
                    loginViewModel.login()
                }
            }
        })

        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.LOADING -> {
                    displayProgressBar(true)
                }
                Status.ERROR -> {
                    displayProgressBar(false)
                    Snackbar.make(view, "Failed: ${it.message}", 5000).show()
                }

                Status.SUCCESS -> {
                    displayProgressBar(false)
                    navigateToMainActivity()
                }
            }
        })
    }

    private fun navigateToMainActivity() {

        activity?.let {
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.startActivity(intent)
            it.finish()
        }
    }

    private fun displayProgressBar(isDisplayed: Boolean) {
        binding?.progressBar?.visibility = if (isDisplayed) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}