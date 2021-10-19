package ru.ya.cup.service.recorder

import android.os.Handler
import android.os.Looper
import ru.ya.cup.extension.lazyUi
import ru.ya.cup.notifications.NotificationCtrl
import java.util.concurrent.TimeUnit

internal class AutoStopDelegate constructor(
    private val notificationCtrl: NotificationCtrl,
    private val autoStopMessage: String,
    private val stopRecorder: () -> Unit,
    private val stopService: () -> Unit
) {
    private companion object {
        val AUTO_STOP_DELAY = TimeUnit.MINUTES.toMillis(30)
        val DELAY: Long = TimeUnit.MINUTES.toMillis(1)
        val STOP_SERVICE_DELAY: Long = TimeUnit.SECONDS.toMillis(3)
    }

    private var startTime: Long = 0

    private val handler: Handler by lazyUi {
        Handler(Looper.getMainLooper())
    }

    private val runnable: Runnable by lazyUi {
        Runnable {
            val currentTime = System.currentTimeMillis()
            if (checkForStop(currentTime)) {
                return@Runnable
            }
            updateNotification(currentTime)
            handler.postDelayed(runnable, DELAY)
        }
    }

    fun setupAutoStop() {
        removeAutoStop()
        startTime = System.currentTimeMillis()
        handler.postDelayed(runnable, DELAY)
        updateNotification(startTime)
    }

    fun removeAutoStop() {
        notificationCtrl.updateRecorderNotification(null)
        handler.removeCallbacks(runnable)
    }

    fun release() {
        handler.removeCallbacks(runnable)
    }

    private fun checkForStop(currentTime: Long): Boolean {
        if (currentTime - startTime >= AUTO_STOP_DELAY - DELAY) {
            stopRecorder()
            handler.postDelayed(stopService, STOP_SERVICE_DELAY)
            return true
        }
        return false
    }

    private fun updateNotification(currentTime: Long) {
        val dif = AUTO_STOP_DELAY - (currentTime - startTime)
        val minutes: Long = dif / (1000 * 60) % 60

        val content = autoStopMessage.format(minutes)
        notificationCtrl.updateRecorderNotification(content)
    }
}
