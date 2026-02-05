package com.brittytino.patchwork.services.tiles

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.util.Log
import com.brittytino.patchwork.R
import com.brittytino.patchwork.utils.DeviceCompat

class UiBlurTileService : BaseTileService() {

    companion object {
        private const val TAG = "UiBlurTileService"
    }

    override fun getTileLabel(): String = "UI Blur"

    override fun getTileSubtitle(): String {
        return if (isBlurEnabled()) "On" else if (DeviceCompat.isBlurSupported(this)) "Off" else "Not Supported"
    }

    override fun hasFeaturePermission(): Boolean {
        return checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }

    override fun getTileIcon(): Icon {
        val iconRes = if (isBlurEnabled()) R.drawable.rounded_blur_on_24 else R.drawable.rounded_blur_off_24
        return Icon.createWithResource(this, iconRes)
    }

    override fun getTileState(): Int {
        return if (!DeviceCompat.isBlurSupported(this)) {
            Tile.STATE_UNAVAILABLE
        } else if (isBlurEnabled()) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
    }

    override fun onTileClick() {
        if (!DeviceCompat.isBlurSupported(this)) {
            Log.w(TAG, "Blur not supported on ${DeviceCompat.getDeviceInfo()}")
            return
        }
        
        try {
            val newState = if (isBlurEnabled()) 1 else 0 // 1 = disable blurs, 0 = enable blurs
            Settings.Global.putInt(contentResolver, "disable_window_blurs", newState)
            DeviceCompat.logFeatureCapability("UI Blur", true, "Toggle successful")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle blur on ${DeviceCompat.getDeviceInfo()}", e)
            // Fallback: Try accessibility reduce transparency setting
            try {
                val currentValue = Settings.Secure.getInt(contentResolver, "accessibility_reduce_transparency", 0)
                Settings.Secure.putInt(contentResolver, "accessibility_reduce_transparency", if (currentValue == 0) 1 else 0)
                DeviceCompat.logFeatureCapability("UI Blur (Fallback)", true, "Using accessibility transparency")
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Fallback also failed", fallbackError)
            }
        }
    }

    private fun isBlurEnabled(): Boolean {
        return try {
            // Try primary method: disable_window_blurs
            val disableBlurs = Settings.Global.getInt(contentResolver, "disable_window_blurs", 0)
            disableBlurs == 0
        } catch (e: Exception) {
            // Fallback: Check accessibility reduce transparency (inverted logic)
            try {
                val reduceTransparency = Settings.Secure.getInt(contentResolver, "accessibility_reduce_transparency", 0)
                reduceTransparency == 0 // 0 means transparency enabled (blur enabled)
            } catch (fallbackError: Exception) {
                false
            }
        }
    }
}
