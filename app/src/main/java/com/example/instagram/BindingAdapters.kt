package com.example.instagram

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
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
    val timeSinceCreated = System.currentTimeMillis() - millis
    when {
        timeSinceCreated < 60 * 1000L -> { // < 1p
            text = context.resources.getQuantityString(
                R.plurals.dateInSecond,
                (timeSinceCreated / 1000).toInt(), timeSinceCreated / 1000
            )
        }
        timeSinceCreated < 60 * 60 * 1000L -> { // < 1h
            text = context.resources.getQuantityString(
                R.plurals.dateInMinute,
                (timeSinceCreated / 1000 / 60).toInt(), timeSinceCreated / 1000 / 60
            )
        }
        timeSinceCreated < 24 * 60 * 60 * 1000L -> { // < 1d
            text = context.resources.getQuantityString(
                R.plurals.dateInHour,
                (timeSinceCreated / 1000 / 60 / 60).toInt(), timeSinceCreated / 1000 / 60 / 60
            )
        }
        timeSinceCreated < 7 * 24 * 60 * 60 * 1000L -> { // <1w
            text = context.resources.getQuantityString(
                R.plurals.dateInDay,
                (timeSinceCreated / 1000 / 60 / 60 / 24).toInt(),
                timeSinceCreated / 1000 / 60 / 60 / 24
            )
        }
        timeSinceCreated < 4 * 7 * 24 * 60 * 60 * 1000L -> {
            text = context.resources.getQuantityString(
                R.plurals.dateInWeek,
                (timeSinceCreated / 1000 / 60 / 60 / 24 / 7).toInt(),
                timeSinceCreated / 1000 / 60 / 60 / 24 / 7
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

@BindingAdapter("link")
fun TextView.setWebLink(link: String?) {
    link?.let {
        val html = "<a href=\'$link\'>$link</a>"
//        val html = resources.getString(R.string.user_website, link)
        movementMethod = LinkMovementMethod.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            text = Html.fromHtml(html)
        }
        Log.e("BindingAdapters", "setWebLink: $text")
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