package com.custom.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioManager
import com.custom.app.R
import com.custom.app.ui.setting.SettingActivity
import com.custom.app.ui.setting.SettingManager
import com.custom.app.util.Constant.NOTIFICATION_STATUS_CHANNEL
import com.custom.app.util.Constant.NOTIFICATION_STATUS_ID
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHandler @Inject internal constructor(
    private val context: Context,
    private val settings: SettingManager,
    private val audioManager: AudioManager,
    private val notificationManager: NotificationManager
) {
    private val res = context.resources

    fun createChannels() {
        val statusChannel = NotificationChannel(
            NOTIFICATION_STATUS_CHANNEL,
            res.getText(R.string.notif_channel_status_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(statusChannel)
    }

    fun updateStatusNotification() {
        if (notificationManager.areNotificationsEnabled()) {
            Timber.d("Updating status notification")
            notificationManager.notify(NOTIFICATION_STATUS_ID, createStatusNotification())
        } else {
            Timber.d("Notifications disabled, not updating status notification")
        }
    }

    fun createStatusNotification(): Notification {
        val playbackConfigs = audioManager.activePlaybackConfigurations
        val totalStreams = playbackConfigs.size

        return Notification.Builder(context, NOTIFICATION_STATUS_CHANNEL).apply {
            setSmallIcon(R.drawable.ic_pause)
            if (totalStreams > 0) {
                setContentTitle("Music is playing")
            } else {
                setContentTitle("Music is not playing")
            }
            when (settings.getDeviceStatus()) {
                1 -> setContentText("Device is connecting..")
                2 -> setContentText("Device is connected!")
                else -> setContentText("Device is disconnected!")
            }
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, SettingActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            addAction(
                buildAction(
                    R.drawable.ic_pause,
                    R.string.btn_restart,
                    AutoPauseService.ACTION_RESTART
                )
            )
            addAction(
                buildAction(
                    R.drawable.ic_pause,
                    R.string.btn_disable,
                    AutoPauseService.ACTION_DISABLE
                )
            )
            setOngoing(true)
        }.build()
    }

    private fun buildAction(icon: Int, title: Int, action: String): Notification.Action {
        return Notification.Action.Builder(
            Icon.createWithResource(context, icon), res.getText(title),
            PendingIntent.getService(
                context, 0, Intent(
                    action, null, context, AutoPauseService::class.java
                ), PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
    }

    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
}