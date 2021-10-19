package ru.ya.cup.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface HeadsetService {
    fun headsetConnectionFlow(): Flow<HeadsetState>
}

internal class HeadsetServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HeadsetService {

    private var lastWiredState: Boolean = false
    private var lastBluetoothState: Boolean = false

    override fun headsetConnectionFlow(): Flow<HeadsetState> {
        return callbackFlow {
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_HEADSET_PLUG -> {
                            val state = intent.getIntExtra("state", -1)
                            val newWiredState = state == 1
                            if (lastWiredState != newWiredState) {
                                trySend(HeadsetState(newWiredState, null))
                                lastWiredState = newWiredState
                            }
                        }
                        BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                            val state = intent.getIntExtra(
                                BluetoothHeadset.EXTRA_STATE,
                                BluetoothHeadset.STATE_DISCONNECTED
                            )
                            val newBluetoothState = state == BluetoothHeadset.STATE_CONNECTED
                            if (lastBluetoothState != newBluetoothState) {
                                trySend(HeadsetState(null, newBluetoothState))
                                lastBluetoothState = newBluetoothState
                            }
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }
            }
            trySend(
                HeadsetState(
                    wiredHeadset = checkHeadsets().also {
                        lastWiredState = it
                    },
                    bluetoothHeadset = checkBluetoothHeadsets().also {
                        lastBluetoothState = it
                    }
                )
            )
            context.registerReceiver(broadcastReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_HEADSET_PLUG)
                addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            })
            awaitClose {
                context.unregisterReceiver(broadcastReceiver)
            }
        }
    }

    private fun checkHeadsets(): Boolean {
        return (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.let {
            it.getDevices(AudioManager.GET_DEVICES_OUTPUTS or AudioManager.GET_DEVICES_INPUTS)
                .toList()
                .any { audioDeviceInfo -> audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET }
        } ?: false
    }

    private fun checkBluetoothHeadsets(): Boolean {
        return BluetoothAdapter.getDefaultAdapter()?.let { adapter ->
            adapter.isEnabled && adapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED
        } ?: false
    }
}

data class HeadsetState(
    val wiredHeadset: Boolean? = null,
    val bluetoothHeadset: Boolean? = null
)

@Module
@InstallIn(SingletonComponent::class)
internal abstract class Module {
    @Binds
    abstract fun bindHeadsetService(impl: HeadsetServiceImpl): HeadsetService
}