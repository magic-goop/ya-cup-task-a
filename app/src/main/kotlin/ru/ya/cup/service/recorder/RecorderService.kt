package ru.ya.cup.service.recorder

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ActivityCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.ya.cup.R
import ru.ya.cup.extension.lazyUi
import ru.ya.cup.notifications.NotificationCtrl
import ru.ya.cup.recorder.Recorder
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RecorderService : Service() {
    companion object {
        const val EXTRA_ID = "sessionId"
        const val EXTRA_COMMAND = "command"

        private const val COMMAND_START = 0
        private const val COMMAND_STOP = 1
        const val COMMAND_SET_AUTO_STOP = 2
        const val COMMAND_REMOVE_AUTO_STOP = 3
        const val COMMAND_HEALTH_CHECK = 4

        fun startRecording(context: Context, sessionId: Long) {
            context.startService(Intent(context, RecorderService::class.java).apply {
                putExtra(EXTRA_ID, sessionId)
                putExtra(EXTRA_COMMAND, COMMAND_START)
            })
        }

        fun stopRecording(context: Context) {
            context.startService(Intent(context, RecorderService::class.java).apply {
                putExtra(EXTRA_COMMAND, COMMAND_STOP)
            })
        }

        private val WAKE_LOCK_TIME_OUT = TimeUnit.MINUTES.toMillis(30)
    }

    @Inject
    lateinit var notificationCtrl: NotificationCtrl

    @Inject
    lateinit var serviceDelegate: RecorderServiceDelegate

    @Inject
    lateinit var recorder: Recorder

    private var wakeLock: PowerManager.WakeLock? = null
    private var sessionId: Long = 0
    private var autoStopDelegate: AutoStopDelegate? = null

    private val receiver: BroadcastReceiver by lazyUi {
        AdapterBroadcastReceiver() { intent ->
            when (intent?.getIntExtra(EXTRA_COMMAND, -1)) {
                COMMAND_SET_AUTO_STOP -> {
                    Timber.d("COMMAND_SET_AUTO_STOP")
                    maybeSetAutoStop()
                }
                COMMAND_REMOVE_AUTO_STOP -> {
                    Timber.d("COMMAND_REMOVE_AUTO_STOP")
                    removeAutoStop()
                }
                COMMAND_HEALTH_CHECK -> {
                    Timber.d("COMMAND_HEALTH_CHECK")
                    serviceDelegate.healthCheckResponse(sessionId)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startServiceForeground()
        setupWakeLock()
        serviceDelegate.registerServiceReceiver(receiver)
    }

    override fun onDestroy() {
        serviceDelegate.healthCheckResponse(sessionId)
        serviceDelegate.unregisterReceiver(receiver)
        stopRecording()
        removeAutoStop()
        wakeLock?.release()
        recorder.release()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopRecording()
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent.getIntExtra(EXTRA_COMMAND, -1)) {
            COMMAND_START -> {
                Timber.d("COMMAND_START")
                if (isPermissionGranted() && recorder.isRecording().not()) {
                    sessionId = intent.getLongExtra(EXTRA_ID, 0L)
                    if (sessionId == 0L) {
                        throw IllegalArgumentException("No sessionId provided")
                    }
                    startRecording()
                    return START_REDELIVER_INTENT
                }
            }
            COMMAND_STOP -> {
                Timber.d("COMMAND_STOP")
                stopRecording()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startServiceForeground() {
        startForeground(
            NotificationCtrl.RECORDER_NOTIF_ID, notificationCtrl.getRecorderNotification()
        )
    }

    private fun setupWakeLock() {
        val powerManager: PowerManager? = getSystemService(PowerManager::class.java)
        powerManager?.let {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.name)
            wakeLock?.setReferenceCounted(false)
        }
    }

    private fun isPermissionGranted(): Boolean =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

    private fun startRecording() {
        wakeLock?.acquire(WAKE_LOCK_TIME_OUT)
        recorder.startRecording(sessionId)
    }

    private fun stopRecording() {
        sessionId = 0
        removeAutoStop()
        recorder.stopRecording()
    }

    private fun maybeSetAutoStop() {
        if (sessionId > 0 && autoStopDelegate == null) {
            val autoStopMessage = getString(R.string.recorder_notification_content_text_auto_close)
            autoStopDelegate = AutoStopDelegate(
                notificationCtrl,
                autoStopMessage,
                ::stopRecording,
                ::stopSelf
            )
        }
        autoStopDelegate?.setupAutoStop()
    }

    private fun removeAutoStop() {
        autoStopDelegate?.removeAutoStop()
        autoStopDelegate = null
    }
}
