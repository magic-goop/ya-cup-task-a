package ru.ya.cup.notifications.builders

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ru.ya.cup.MainActivity
import ru.ya.cup.R
import javax.inject.Inject

internal class RecorderNotificationBuilder @Inject constructor() {
    fun build(context: Context, contentText: String? = null): Notification = NotificationCompat
        .Builder(
            context,
            context.getString(R.string.recorder_notification_channel_id)
        )
        .setAutoCancel(false)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setOngoing(true)
        .setSound(null)
        .setSmallIcon(R.drawable.ic_mic_24)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText(
            contentText ?: context.getString(R.string.recorder_notification_content_text)
        )
        .setContentIntent(
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .build()
}
