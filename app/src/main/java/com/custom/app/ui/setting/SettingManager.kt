package com.custom.app.ui.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.custom.app.R
import com.custom.app.data.BooleanPreference
import com.custom.app.data.IntPreference
import com.custom.app.data.StringPreference
import com.custom.app.util.Constant.KEY_DEFAULT_DEVICE
import com.custom.app.util.Constant.KEY_DEVICE_NAME
import com.custom.app.util.Constant.KEY_DEVICE_STATUS
import com.custom.app.util.Constant.KEY_IGNORE_VOLUME
import com.custom.app.util.Constant.KEY_REBOOT_ENABLED
import com.custom.app.util.Constant.KEY_SERVICE_ENABLED
import com.custom.app.util.Constant.KEY_UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingManager @Inject internal constructor(
    private val context: Context,
    private val prefs: SharedPreferences,
    @Singleton @Named(KEY_DEVICE_STATUS) private var deviceStatusPref: IntPreference,
    @Singleton @Named(KEY_IGNORE_VOLUME) private var ignoreVolumePref: BooleanPreference,
    @Singleton @Named(KEY_SERVICE_ENABLED) private var serviceEnabledPref: BooleanPreference,
    @Singleton @Named(KEY_REBOOT_ENABLED) private var rebootEnabledPref: BooleanPreference,
    @Singleton @Named(KEY_DEFAULT_DEVICE) private var defaultDevicePref: StringPreference,
    @Singleton @Named(KEY_DEVICE_NAME) private var deviceNamePref: StringPreference,
    @Singleton @Named(KEY_UUID) private var uuIdPref: StringPreference
) : SharedPreferences.OnSharedPreferenceChangeListener {

    private val listeners = mutableSetOf<ChangeListener>()

    init {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false)

        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    interface ChangeListener {
        fun onSettingsChanged(key: String)
        fun onSettingsCleared()
    }

    fun addChangeListener(listener: ChangeListener) {
        listeners.add(listener)
    }

    fun removeChangeListener(listener: ChangeListener) {
        listeners.remove(listener)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        for (listener in listeners) {
            if (key != null) {
                listener.onSettingsChanged(key)
            } else {
                listener.onSettingsCleared()
            }
        }
    }

    fun getDeviceStatus(): Int {
        return deviceStatusPref.get()
    }

    fun setDeviceStatus(state: Int) {
        deviceStatusPref.set(state)
    }

    fun ignoreVolume(): Boolean {
        return ignoreVolumePref.get()
    }

    fun ignoreVolume(status: Boolean) {
        ignoreVolumePref.set(status)
    }

    fun serviceEnabled(): Boolean {
        return serviceEnabledPref.get()
    }

    fun serviceEnabled(status: Boolean) {
        serviceEnabledPref.set(status)
    }

    fun rebootEnabled(): Boolean {
        return rebootEnabledPref.get()
    }

    fun rebootEnabled(status: Boolean) {
        rebootEnabledPref.set(status)
    }

    fun defaultDevice(): String {
        return defaultDevicePref.get()
    }

    fun deviceName(): String {
        return deviceNamePref.get()
    }

    fun deviceName(name: String) {
        deviceNamePref.set(name)
    }

    fun uuId(): String {
        return uuIdPref.get()
    }

    fun uuId(name: String) {
        uuIdPref.set(name)
    }

    fun clear() {
        serviceEnabledPref.delete()
        rebootEnabledPref.delete()
        defaultDevicePref.delete()
        deviceStatusPref.delete()
        ignoreVolumePref.delete()
        deviceNamePref.delete()
        uuIdPref.delete()
    }
}