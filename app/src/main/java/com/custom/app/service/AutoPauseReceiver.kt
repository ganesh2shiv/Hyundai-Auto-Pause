package com.custom.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.custom.app.ui.setting.SettingManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutoPauseReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settings: SettingManager

    override fun onReceive(context: Context, intent: Intent) {
        if (settings.serviceEnabled() && settings.rebootEnabled()) {
            AutoPauseService.start(context, intent.action == Intent.ACTION_BOOT_COMPLETED)
        }
    }
}