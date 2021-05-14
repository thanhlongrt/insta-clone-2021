package com.example.instagram.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.R
import com.example.instagram.Status
import com.example.instagram.databinding.FragmentNotificationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Created by Thanh Long Nguyen on 4/12/2021
 */

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotificationFragment : Fragment() {
    companion object {
        private const val TAG = "NotificationFragment"
    }

    private var binding: FragmentNotificationBinding? = null

    private val notificationViewModel: NotificationViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationViewModel.uploadFcmToken()
        notificationViewModel.getNotification()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            FragmentNotificationBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        createChannel(
            getString(R.string.post_notification_channel_id),
            getString(R.string.post_notification_channel_name)
        )

        notificationAdapter = NotificationAdapter(mutableListOf())
        notificationAdapter.onClick = { position, notification ->
            notificationAdapter.seen(position)
        }
        binding?.recyclerView?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = notificationAdapter
        }

        notificationViewModel.notifications.observe(requireActivity()) {
            if (it.status == Status.SUCCESS) {
                notificationAdapter.addAll(it.data!!)
            }
        }

    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.enableLights(true)
            channel.lightColor = Color.CYAN
            channel.enableVibration(true)
            channel.description = "Channel description"

            val notificationManager =
                requireActivity().getSystemService(NotificationManager::class.java)
                        as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}