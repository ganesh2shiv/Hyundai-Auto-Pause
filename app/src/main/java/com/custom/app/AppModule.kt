package com.custom.app

import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import com.custom.app.service.BluetoothService
import com.custom.app.service.NotificationHandler
import com.custom.app.ui.setting.SettingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideMainThreadHandler() = Handler(Looper.getMainLooper())

    @Provides
    @Singleton
    fun provideAudioFocusRequest(): AudioFocusRequest {
        return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            ).build()
    }

    @Provides
    @Singleton
    fun provideAudioManager(context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Provides
    @Singleton
    fun provideBluetoothManager(context: Context): BluetoothManager {
        return (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
    }

    @Provides
    @Singleton
    fun provideNotificationHandler(context: Context,
                                   settings: SettingManager,
                                   audioManager: AudioManager): NotificationHandler {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return NotificationHandler(context, settings, audioManager, manager)
    }

    @Provides
    @Singleton
    fun provideBluetoothService(context: Context): BluetoothService {
        return BluetoothService(context)
    }
}