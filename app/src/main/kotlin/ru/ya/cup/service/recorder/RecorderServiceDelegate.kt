package ru.ya.cup.service.recorder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.ya.cup.service.recorder.RecorderService.Companion.COMMAND_HEALTH_CHECK
import ru.ya.cup.service.recorder.RecorderService.Companion.COMMAND_REMOVE_AUTO_STOP
import ru.ya.cup.service.recorder.RecorderService.Companion.COMMAND_SET_AUTO_STOP
import ru.ya.cup.service.recorder.RecorderService.Companion.EXTRA_COMMAND
import ru.ya.cup.service.recorder.RecorderService.Companion.EXTRA_ID
import javax.inject.Inject
import javax.inject.Singleton

interface RecorderServiceDelegate {
    fun startRecorderService(sessionId: Long)
    fun stopRecorderService()

    fun registerServiceReceiver(receiver: BroadcastReceiver)
    fun registerClientReceiver(receiver: BroadcastReceiver)

    fun unregisterReceiver(receiver: BroadcastReceiver)

    fun healthCheck()
    fun healthCheckResponse(sessionId: Long)

    fun setAutoStop()
    fun removeAutoStop()
}

internal class RecorderServiceDelegateImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RecorderServiceDelegate {

    private companion object {
        val SERVICE_ACTION = "${RecorderServiceDelegate::class.java}_service"
        val CLIENT_ACTION = "${RecorderServiceDelegate::class.java}_client"
    }

    override fun startRecorderService(sessionId: Long) {
        RecorderService.startRecording(context, sessionId)
    }

    override fun stopRecorderService() {
        RecorderService.stopRecording(context)
    }

    override fun registerServiceReceiver(receiver: BroadcastReceiver) {
        LocalBroadcastManager
            .getInstance(context)
            .registerReceiver(receiver, IntentFilter(SERVICE_ACTION))
    }

    override fun registerClientReceiver(receiver: BroadcastReceiver) {
        LocalBroadcastManager
            .getInstance(context)
            .registerReceiver(receiver, IntentFilter(CLIENT_ACTION))
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver) {
        LocalBroadcastManager
            .getInstance(context)
            .unregisterReceiver(receiver)
    }

    override fun healthCheck() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(SERVICE_ACTION).apply {
                putExtra(EXTRA_COMMAND, COMMAND_HEALTH_CHECK)
            })
    }

    override fun healthCheckResponse(sessionId: Long) {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(CLIENT_ACTION).apply {
                putExtra(EXTRA_ID, sessionId)
            })
    }

    override fun setAutoStop() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(SERVICE_ACTION).apply {
                putExtra(EXTRA_COMMAND, COMMAND_SET_AUTO_STOP)
            })
    }

    override fun removeAutoStop() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(SERVICE_ACTION).apply {
                putExtra(EXTRA_COMMAND, COMMAND_REMOVE_AUTO_STOP)
            })
    }
}

class AdapterBroadcastReceiver(private val action: (Intent?) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        action(intent)
    }
}


@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecorderServiceDelegateModule {
    @Binds
    @Singleton
    abstract fun bindDelegate(impl: RecorderServiceDelegateImpl): RecorderServiceDelegate
}

