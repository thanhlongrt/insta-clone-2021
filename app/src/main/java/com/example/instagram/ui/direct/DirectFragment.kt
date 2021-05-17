package com.example.instagram.ui.direct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.instagram.databinding.FragmentDirectBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Thanh Long Nguyen on 5/16/2021
 */

@AndroidEntryPoint
class DirectFragment : Fragment() {

    companion object {
        private const val TAG = "DirectFragment"
    }

    private var binding: FragmentDirectBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDirectBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}