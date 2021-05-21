package com.example.instagram.extensions

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.instagram.R
import java.text.NumberFormat
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
            val likeCountString = NumberFormat.getInstance().format(likeCount)
            text =
                resources.getQuantityString(
                    R.plurals.likeCount,
                    likeCount.toInt(),
                    likeCountString
                )
        }
    }
}

@BindingAdapter("set_comment_count")
fun TextView.setCommentCount(commentCount: Long) {
    val commentCountString = NumberFormat.getInstance().format(commentCount)
    text = resources.getQuantityString(
        R.plurals.commentCount,
        commentCount.toInt(),
        commentCountString
    )
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
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }
}

@BindingAdapter("follow_state")
fun CheckBox.setFollowState(isFollowed: Boolean) {
    text = if (isFollowed) {
        context.getString(R.string.following)
    } else {
        resources.getString(R.string.follow)
    }
}