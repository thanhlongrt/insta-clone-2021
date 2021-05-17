package com.example.instagram.ui.reel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.instagram.databinding.FragmentReelBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by Thanh Long Nguyen on 5/16/2021
 */

@AndroidEntryPoint
class ReelFragment : Fragment() {
    companion object {
        private const val TAG = "ReelFragment"
    }

    private var binding: FragmentReelBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReelBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}