package com.example.instagram.ui.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.instagram.R
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Thanh Long Nguyen on 5/13/2021
 */
@BindingAdapter("image_url")
fun ImageView.loadFromUrl(url: String?) {
    if (!url.isNullOrEmpty()) {
        Glide.with(this.context)
            .load(url)
            .into(this)
    }
}

@BindingAdapter("set_like_count")
fun TextView.setLikeCount(likeCount: Long) {
    when (likeCount) {
        0L -> {
            visibility = View.GONE
        }
        else -> {
            visibility = View.VISIBLE
            text = resources.getQuantityString(R.plurals.likeCount, likeCount.toInt(), likeCount)
        }
    }
}

@SuppressLint("SimpleDateFormat")
@BindingAdapter("date_from_millis")
fun TextView.setDate(millis: Long) {
    val period = System.currentTimeMillis() - millis
    when {
        period < 60 * 1000L -> { // < 1p
            text = context.resources.getQuantityString(
                R.plurals.dateInSecond,
                (period / 1000).toInt(), period / 1000
            )
        }
        period < 60 * 60 * 1000L -> { // < 1h
            text = context.resources.getQuantityString(
                R.plurals.dateInMinute,
                (period / 1000 / 60).toInt(), period / 1000 / 60
            )
        }
        period < 24 * 60 * 60 * 1000L -> { // < 1d
            text = context.resources.getQuantityString(
                R.plurals.dateInHour,
                (period / 1000 / 60 / 60).toInt(), period / 1000 / 60 / 60
            )
        }
        period < 7 * 24 * 60 * 60 * 1000L -> { // <1w
            text = context.resources.getQuantityString(
                R.plurals.dateInDay,
                (period / 1000 / 60 / 60 / 24).toInt(), period / 1000 / 60 / 60 / 24
            )
        }
        period < 4 * 7 * 24 * 60 * 60 * 1000L -> {
            text = context.resources.getQuantityString(
                R.plurals.dateInWeek,
                (period / 1000 / 60 / 60 / 24 / 7).toInt(), period / 1000 / 60 / 60 / 24 / 7
            )
        }
        else -> {
            val date = Date(millis)
            val simpleDateFormat = SimpleDateFormat("dd MMM yyyy")
            val dateStr = simpleDateFormat.format(date)
            this.text = dateStr
        }
    }
}

@BindingAdapter("video_url")
fun PlayerView.loadVideo(url: String?) {
    Log.e("BindingAdapters", "loadVideo: starting...")
    if (url == null) return
    val player = SimpleExoPlayer.Builder(
        context,
        DefaultRenderersFactory(context)
    )
        .setTrackSelector(DefaultTrackSelector(context))
        .setLoadControl(DefaultLoadControl())
        .build()
    player.playWhenReady = true
    player.repeatMode = Player.REPEAT_MODE_ONE
//    setKeepContentOnPlayerReset(true)
    useController = false

    val mediaItem = MediaItem.Builder().setUri(Uri.parse(url)).build()

    val mediaSource = ProgressiveMediaSource.Factory(
        DefaultHttpDataSource.Factory()
    ).createMediaSource(mediaItem)

    this.player = player
    player.setMediaSource(mediaSource)
    player.prepare()
    Log.e("BindingAdapters", "loadVideo: ")
}