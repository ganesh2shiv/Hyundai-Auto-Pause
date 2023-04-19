package com.custom.app.ui.setting

import android.Manifest
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.custom.app.BuildConfig
import com.custom.app.R
import com.custom.app.util.AlertUtil
import com.custom.app.util.Util
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingActivity : AppCompatActivity(), SettingFragment.Callback  {

    private var disposable: Disposable? = null
    private lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(android.R.id.content, SettingFragment())
            }
        }

        rxPermissions = RxPermissions(this)
        rxPermissions.setLogging(BuildConfig.DEBUG);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            disposable = rxPermissions
                .requestEachCombined(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
                .delay(1500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ permission ->
                    if (permission.granted) {
                        Timber.d("Permission granted!")
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        showMessage(getString(R.string.permission_error_msg))
                    } else {
                        AlertUtil.showActionAlertDialog(this,
                            getString(R.string.request_permission_msg, "Nearby devices"),
                            getString(R.string.btn_cancel), getString(R.string.btn_ok)
                        ) { _, _ -> Util.showAppSettings(this, packageName) }
                    }
                }) { error ->
                    Timber.e(error)
                    showMessage(getString(R.string.unknown_error_msg))
                }
        }
    }

    private fun showMessage(msg: String?) {
        AlertUtil.showToast(this, msg)
    }

    override fun onRestartApp() {
        val taskBuilder = TaskStackBuilder.create(this)
            .addNextIntent(Intent(this, SettingActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        taskBuilder.startActivities()
    }

    override fun onDestroy() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable?.dispose()
        }
        super.onDestroy()
    }
}