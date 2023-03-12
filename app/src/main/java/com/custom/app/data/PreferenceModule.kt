package com.custom.app.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.custom.app.util.Constant.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PreferenceModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val keyGenParamSpec = KeyGenParameterSpec.Builder(
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                    .build()
                MasterKey.Builder(context)
                    .setKeyGenParameterSpec(keyGenParamSpec)
                    .build()
            } else {
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            }
            EncryptedSharedPreferences.create(
                context,
                "preferences",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e)
            throw RuntimeException(e)
        }
    }

    @Provides
    @Named(KEY_SERVICE_ENABLED)
    @Singleton
    fun provideServiceEnabled(prefs: SharedPreferences): BooleanPreference {
        return BooleanPreference(prefs, KEY_SERVICE_ENABLED)
    }

    @Provides
    @Named(KEY_REBOOT_ENABLED)
    @Singleton
    fun provideRebootEnabled(prefs: SharedPreferences): BooleanPreference {
        return BooleanPreference(prefs, KEY_REBOOT_ENABLED)
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