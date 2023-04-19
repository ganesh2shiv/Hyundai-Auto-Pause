package com.custom.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import com.custom.app.monitor.AudioPlaybackMonitor
import com.custom.app.service.BluetoothService.onConnectionListener
import com.custom.app.service.BluetoothService.onReceiveListener
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
class AutoPauseService : Service(), onConnectionListener, onReceiveListener, AudioPlaybackMonitor.Listener {

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
        const val ACTION_RECONNECT = "com.custom.app.action.RECONNECT"
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

        settings.setDeviceStatus(0)

        notifications.createChannels()
        playbackMonitor.addListener(this)

        val statusNotification = notifications.createStatusNotification()
        startForeground(NOTIFICATION_STATUS_ID, statusNotification)

        connect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            Timber.d("Received command: ${intent.action}")
            if (intent.getBooleanExtra(EXTRA_BOOT, false)) {
                Timber.d("System rebooted!")
            } else if (intent.action != null) {
                when (intent.action) {
                    ACTION_RECONNECT -> {
                        connect()
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

    private fun connect() {
        if (bluetoothManager.adapter != null && bluetoothManager.adapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !Util.isPermissionGranted(applicationContext, Manifest.permission.BLUETOOTH_CONNECT)) {
                AlertUtil.showToast(this, "Please allow required permission!")
                return
            }

            val devices = bluetoothManager.adapter.bondedDevices
            if (devices.isNotEmpty()) {
                val car = devices.find { it.name.equals(settings.deviceName()) }
                if (car != null) {
                    val uuid = settings.uuId()
                    bluetoothService.connect(car.address, uuid, this, this)
                } else {
                    AlertUtil.showToast(this, "Please connect the bluetooth!")
                }
            }
        } else {
            AlertUtil.showToast(this, "Please turn on the bluetooth!")
        }
    }

    override fun onConnectionStateChanged(state: Int) {
        when (state) {
            BluetoothService.DISCONNECTED -> disconnect()
            BluetoothService.CONNECTING -> settings.setDeviceStatus(1)
            BluetoothService.CONNECTED -> settings.setDeviceStatus(2)
        }
        updateStatusNotification()
    }

    override fun onConnectionFailed(errorCode: Int) {
        when (errorCode) {
            BluetoothService.SOCKET_NOT_FOUND -> Timber.d("Socket not found")
            BluetoothService.CONNECT_FAILED -> Timber.d("Connect failed")
        }
        disconnect()
        updateStatusNotification()
    }

    override fun onReceived(buffer: ByteArray?) {
        Timber.d("Buffer: %s", buffer?.take(25)?.joinToString(", ", "[", "...]"))
        if (buffer != null && buffer.size > 5) {
            val volume = buffer[5].toInt()
            Timber.d("Volume: ${volume/2}")
            if (volume in 0..90 && buffer[6].toInt() == 124) {
                if (volume == 1 || (!settings.ignoreVolume() && volume == 0)) {
                    if (audioManager.isMusicActive) {
                        audioManager.requestAudioFocus(audioRequest)
                        AlertUtil.showToast(this, "Paused...")
                    }
                } else {
                    if (!audioManager.isMusicActive) {
                        audioManager.abandonAudioFocus(null)
                        AlertUtil.showToast(this, "Playing...")
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
        settings.setDeviceStatus(0)
        bluetoothService.disconnect()
        Timber.d("Disconnect")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy")

        disconnect()
        playbackMonitor.removeListener(this)
    }
}