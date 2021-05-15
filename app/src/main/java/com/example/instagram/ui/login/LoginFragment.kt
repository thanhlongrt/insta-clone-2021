package com.example.instagram.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentLoginBinding
import com.example.instagram.ui.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {
        const val TAG = "LoginFragment"
    }

    private var binding: FragmentLoginBinding? = null

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding?.loginViewModel = loginViewModel
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.navToSignup?.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        loginViewModel.loginFormState.observe(viewLifecycleOwner, Observer {
            val loginFormState = it ?: return@Observer
            binding?.login?.isEnabled = loginFormState.data!!
        })

        binding?.email?.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged()
            }
        }

        binding?.password?.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged()
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        loginViewModel.login()
                    }
                }
                false
            }
        }

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

    private fun displayProgressBar(isDisplayed: Boolean) {
        activity?.let {
            it.findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (isDisplayed) View.VISIBLE else View.GONE
        }
    }

    private fun navigateToMainActivity() {

        activity?.let {
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.startActivity(intent)
            it.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


}