package com.example.instagram

import android.app.NotificationManager
import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import kotlin.math.max

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
        Log.e("AAAAAAAAAAAAAAa", "sendNotification: $it")
        val bitmap = futureTarget.get()
        val largeIcon = bitmap.getCircularBitmap()
        builder.setLargeIcon(largeIcon)
        Glide.with(applicationContext).clear(futureTarget)
    }


    notify(notificationId, builder.build())
}

fun Bitmap.getCircularBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    // circle configuration
    val circlePaint = Paint().apply { isAntiAlias = true }
    val circleRadius = max(width, height) / 2f

    // output bitmap
    val outputBitmapPaint =
        Paint(circlePaint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
    val outputBounds = Rect(0, 0, width, height)
    val output = Bitmap.createBitmap(width, height, config)

    return Canvas(output).run {
        drawCircle(circleRadius, circleRadius, circleRadius, circlePaint)
        drawBitmap(this@getCircularBitmap, outputBounds, outputBounds, outputBitmapPaint)
        output
    }
}