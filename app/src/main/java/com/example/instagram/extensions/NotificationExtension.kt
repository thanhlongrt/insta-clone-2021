package com.example.instagram.extensions

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.instagram.utils.ImageUtils.getCircularBitmap
import com.example.instagram.R

/**
 * Created by Thanh Long Nguyen on 5/7/2021
 */

fun NotificationManager.sendNotification(
    messageBody: String,
    applicationContext: Context,
    largeIconUrl: String?,
    notificationId: Int
) {

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.post_notification_channel_id)
    )
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)

    largeIconUrl?.let {
        val futureTarget = Glide.with(applicationContext)
            .asBitmap()
            .load(it).submit()
        val bitmap = futureTarget.get()
        val largeIcon = bitmap.getCircularBitmap()
        builder.setLargeIcon(largeIcon)
        Glide.with(applicationContext).clear(futureTarget)
    }


    notify(notificationId, builder.build())
}
