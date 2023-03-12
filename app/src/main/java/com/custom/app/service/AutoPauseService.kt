package com.custom.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.IBinder
import com.custom.app.monitor.AudioPlaybackMonitor
import com.custom.app.ui.setting.SettingManager
import com.custom.app.util.AlertUtil
import com.custom.app.util.Constant.NOTIFICATION_STATUS_ID
import com.custom.app.util.Util
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class AutoPauseService : Service(), AudioPlaybackMonitor.Listener {

    @Inject
    lateinit var settings: SettingManager

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var audioRequest: AudioFocusRequest

    @Inject
    lateinit var notifications: NotificationHandler

    @Inject
    lateinit var bluetoothService: BluetoothService

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    @Inject
    lateinit var playbackMonitor: AudioPlaybackMonitor

    companion object {
        const val EXTRA_BOOT = "com.custom.app.extra.BOOT"
        const val ACTION_RESTART = "com.custom.app.action.RESTART"
        const val ACTION_DISABLE = "com.custom.app.action.DISABLE"

        fun start(context: Context, boot: Boolean = false) {
            context.startForegroundService(
                Intent(context, AutoPauseService::class.java).apply {
                    putExtra(EXTRA_BOOT, boot)
                }
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AutoPauseService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("Starting")

        notifications.createChannels()
        playbackMonitor.addListener(this)

        val statusNotification = notifications.createStatusNotification()
        startForeground(NOTIFICATION_STATUS_ID, statusNotification)

        val connectionListener = object : BluetoothService.onConnectionListener {
            override fun onConnectionStateChanged(state: Int) {
                when (state) {
                    BluetoothService.DISCONNECTED -> settings.setDeviceStatus(0)
                    BluetoothService.CONNECTING -> settings.setDeviceStatus(1)
                    BluetoothService.CONNECTED -> settings.setDeviceStatus(2)
                }
                updateStatusNotification()
            }

            override fun onConnectionFailed(errorCode: Int) {
                when (errorCode) {
                    BluetoothService.SOCKET_NOT_FOUND -> {
                        Timber.d("Socket not found")
                    }
                    BluetoothService.CONNECT_FAILED -> {
                        Timber.d("Connect Failed")
                    }
                }
                settings.setDeviceStatus(0)
                updateStatusNotification()
            }
        }

        val receiveListener = BluetoothService.onReceiveListener { buffer ->
            playPauseAudio(buffer)
        }

        if (Util.isPermissionGranted(applicationContext, Manifest.permission.BLUETOOTH_CONNECT)) {
            val devices = bluetoothManager.adapter.bondedDevices
            if (devices.isNotEmpty()) {
                val car = devices.singleOrNull { it.name.equals(settings.deviceName()) }
                if (car != null) {
                    val uuid = settings.uuId()
                    bluetoothService.connect(car, uuid, true, connectionListener, receiveListener)
                }
            }
        } else {
            Timber.e("Permission error")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            Timber.d("Received command: ${intent.action}")
            if (intent.getBooleanExtra(EXTRA_BOOT, false)) {
                Timber.d("System rebooted!")
            } else if (intent.action != null) {
                when (intent.action) {
                    ACTION_RESTART -> {
                        stop(this)
                        start(this)
                    }
                    ACTION_DISABLE -> {
                        settings.serviceEnabled(false)
                        stop(this)
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun playPauseAudio(buffer: ByteArray?) {
        if (buffer != null && buffer.size > 5) {
            Timber.d(buffer.take(25).joinToString(", ", "[", "...]"))
            val volume = buffer[5].toInt()
            Timber.d("Volume: ${volume/2}")
            if (volume in 0..90 && buffer[6].toInt() == 124) {
                if (volume == 1 || (!settings.ignoreVolume() && volume == 0)) {
                    if (audioManager.isMusicActive) {
                        audioManager.requestAudioFocus(audioRequest)
                        AlertUtil.showToast(applicationContext, "Paused...")
                    }
                } else {
                    if (!audioManager.isMusicActive) {
                        audioManager.abandonAudioFocus(null)
                        AlertUtil.showToast(applicationContext, "Playing...")
                    }
                }
            }
        }
    }

    override fun onAudioPlaybacksChanged() {
        updateStatusNotification()
    }

    private fun updateStatusNotification() {
        notifications.updateStatusNotification()
    }

    private fun disconnect() {
        bluetoothService.disconnect()
        Timber.d("Disconnect")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Stopping")

        disconnect()
        playbackMonitor.removeListener(this)
    }
}