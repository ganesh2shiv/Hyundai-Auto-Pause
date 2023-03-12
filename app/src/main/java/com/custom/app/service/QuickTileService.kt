package com.custom.app.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.custom.app.R
import com.custom.app.ui.setting.SettingManager
import com.custom.app.util.Constant.KEY_SERVICE_ENABLED
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuickTileService : TileService(), SettingManager.ChangeListener {

    @Inject
    lateinit var settings: SettingManager

    override fun onStartListening() {
        settings.addChangeListener(this)
        updateState()
    }

    override fun onStopListening() {
        settings.removeChangeListener(this)
    }

    override fun onSettingsChanged(key: String) {
        when (key) {
            KEY_SERVICE_ENABLED -> updateState()
        }
    }

    override fun onSettingsCleared() {
        updateState()
    }

    private fun updateState() {
        qsTile.apply {
            val enabled = settings.serviceEnabled()

            state = if (enabled) {
                Tile.STATE_ACTIVE
            } else {
                Tile.STATE_INACTIVE
            }

            if (Build.VERSION.SDK_INT >= 29) {
                subtitle = resources.getString(
                    if (enabled) {
                        R.string.qs_tile_enable_subtitle_enabled
                    } else {
                        R.string.qs_tile_enable_subtitle_disabled
                    }
                )
            }

            if (Build.VERSION.SDK_INT >= 30) {
                stateDescription = resources.getString(
                    if (enabled) {
                        R.string.qs_tile_enable_state_enabled
                    } else {
                        R.string.qs_tile_enable_state_disabled
                    }
                )
            }
        }.updateTile()
    }

    override fun onClick() {
        settings.serviceEnabled(!settings.serviceEnabled())
        if (settings.serviceEnabled()) {
            AutoPauseService.start(this)
        } else {
            AutoPauseService.stop(this)
        }
    }
}