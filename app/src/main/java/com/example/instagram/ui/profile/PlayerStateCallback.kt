package com.example.instagram.ui.profile

import com.google.android.exoplayer2.Player

/**
 * Created by Thanh Long Nguyen on 5/13/2021
 */
interface PlayerStateCallback {

    fun onVideoDurationRetrieved(duration: Long, player: Player)

    fun onVideoBuffering(player: Player)

    fun onStartedPlaying(player: Player)

    fun onFinishedPlaying(player: Player)

}