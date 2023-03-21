package com.custom.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.custom.app.util.Constant.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PreferenceModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Named(KEY_SERVICE_ENABLED)
    @Singleton
    fun provideServiceEnabled(prefs: SharedPreferences): BooleanPreference {
        return BooleanPreference(prefs, KEY_SERVICE_ENABLED, true)
    }

    @Provides
    @Named(KEY_REBOOT_ENABLED)
    @Singleton
    fun provideRebootEnabled(prefs: SharedPreferences): BooleanPreference {
        return BooleanPreference(prefs, KEY_REBOOT_ENABLED, true)
    }

    @Provides
    @Named(KEY_DEFAULT_DEVICE)
    @Singleton
    fun provideDefaultDevice(prefs: SharedPreferences): StringPreference {
        return StringPreference(prefs, KEY_DEFAULT_DEVICE, DeviceModel.i20.name)
    }

    @Provides
    @Named(KEY_DEVICE_NAME)
    @Singleton
    fun provideDeviceName(prefs: SharedPreferences): StringPreference {
        return StringPreference(prefs, KEY_DEVICE_NAME)
    }

    @Provides
    @Named(KEY_UUID)
    @Singleton
    fun provideUuid(prefs: SharedPreferences): StringPreference {
        return StringPreference(prefs, KEY_UUID)
    }

    @Provides
    @Named(KEY_DEVICE_STATUS)
    @Singleton
    fun provideDeviceStatus(prefs: SharedPreferences): IntPreference {
        return IntPreference(prefs, KEY_DEVICE_STATUS)
    }

    @Provides
    @Named(KEY_IGNORE_VOLUME)
    @Singleton
    fun provideIgnoreVolume(prefs: SharedPreferences): BooleanPreference {
        return BooleanPreference(prefs, KEY_IGNORE_VOLUME)
    }
}