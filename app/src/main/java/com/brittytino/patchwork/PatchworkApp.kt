package com.brittytino.patchwork

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.brittytino.patchwork.services.ScreenOffReceiver
import com.brittytino.patchwork.utils.ShizukuUtils

class PatchworkApp : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    private val screenOffReceiver = ScreenOffReceiver()

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ShizukuUtils.initialize()
        com.brittytino.patchwork.utils.LogManager.init(this)
        
        // Init Action History Logger
        com.brittytino.patchwork.utils.ActionLogger.init(this)
        
        // Init Automation
        com.brittytino.patchwork.domain.diy.DIYRepository.init(this)
        com.brittytino.patchwork.services.automation.AutomationManager.init(this)
        
        // Init Idle App Monitor if enabled
        val prefs = getSharedPreferences("essentials_prefs", Context.MODE_PRIVATE)
        val isIdleAppEnabled = prefs.getBoolean("idle_app_auto_action_enabled", false)
        if (isIdleAppEnabled) {
            com.brittytino.patchwork.services.IdleAppEngine.getInstance(this).startMonitoring()
        }

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenOffReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(screenOffReceiver, intentFilter)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(screenOffReceiver)
    }
}

