package ru.ya.cup.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.ya.cup.R
import ru.ya.cup.notifications.NotificationCtrl.Companion.RECORDER_NOTIF_ID
import ru.ya.cup.notifications.builders.RecorderNotificationBuilder
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationCtrl {
    companion object {
        const val RECORDER_NOTIF_ID = 1
    }

    fun init()
    fun getRecorderNotification(): Notification
    fun updateRecorderNotification(contentText: String?)
}

internal class NotificationCtrlImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recorderBuilder: RecorderNotificationBuilder
) : NotificationCtrl {

    private var isInitialized: Boolean = false

    private val notificationManager: NotificationManager? =
        context.getSystemService(NotificationManager::class.java)

    override fun init() {
        isInitialized = true
        val resources: Resources = context.resources
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val recorderChannel = NotificationChannel(
                resources.getString(R.string.recorder_notification_channel_id),
                resources.getString(R.string.recorder_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
            }
            try {
                notificationManager?.run {
                    createNotificationChannel(recorderChannel)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun getRecorderNotification(): Notification {
        initialisationCheck()
        return recorderBuilder.build(context, null)
    }

    override fun updateRecorderNotification(contentText: String?) {
        initialisationCheck()
        showNotification(RECORDER_NOTIF_ID, recorderBuilder.build(context, contentText))
    }

    private fun showNotification(id: Int, notification: Notification) =
        notificationManager?.notify(id, notification)

    private fun initialisationCheck() {
        if (!isInitialized) {
            throw IllegalStateException("call init() first")
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal class Module {

    @Provides
    @Singleton
    fun provideCtrl(@ApplicationContext context: Context): NotificationCtrl =
        NotificationCtrlImpl(context, RecorderNotificationBuilder())
}
