package com.brittytino.patchwork.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Device compatibility checker for universal feature support across all Android devices.
 * Provides graceful fallbacks for OEM-specific features.
 */
object DeviceCompat {
    private const val TAG = "DeviceCompat"

    /**
     * Check if system blur effects are supported on this device.
     * Works on: Pixel (Android 12+), Samsung (OneUI 4+), some custom ROMs
     */
    fun isBlurSupported(context: Context): Boolean {
        return try {
            // Try to read blur setting - if it exists, blur is supported
            val blurEnabled = Settings.Secure.getInt(
                context.contentResolver,
                "accessibility_reduce_transparency",
                -1
            )
            blurEnabled != -1 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        } catch (e: Exception) {
            Log.w(TAG, "Blur check failed", e)
            false
        }
    }

    /**
     * Check if Night Light/Blue Light filter is supported.
     * Most devices since Android 8.0 support this.
     */
    fun isNightLightSupported(context: Context): Boolean {
        return try {
            // Try multiple common secure settings names used by different OEMs
            val settings = listOf(
                "night_display_activated",           // AOSP/Pixel
                "blue_light_filter",                  // Samsung
                "reading_mode_status",                // Xiaomi
                "eye_comfort_shield",                 // Samsung OneUI
                "dc_back_light",                      // OnePlus
                "comfort_view_enabled"                // Motorola
            )
            
            settings.any { setting ->
                try {
                    Settings.Secure.getInt(context.contentResolver, setting, -1) != -1
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Night light check failed", e)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O // Fallback to API level
        }
    }

    /**
     * Get Night Light setting name for this device
     */
    fun getNightLightSettingName(context: Context): String {
        val settings = mapOf(
            "night_display_activated" to "AOSP/Pixel",
            "blue_light_filter" to "Samsung",
            "reading_mode_status" to "Xiaomi",
            "eye_comfort_shield" to "Samsung OneUI",
            "dc_back_light" to "OnePlus",
            "comfort_view_enabled" to "Motorola"
        )
        
        for ((setting, _) in settings) {
            try {
                if (Settings.Secure.getInt(context.contentResolver, setting, -1) != -1) {
                    return setting
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return "night_display_activated" // Default AOSP
    }

    /**
     * Check if flashlight intensity control is supported
     */
    fun isFlashlightIntensitySupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return false
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val maxLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 0
            maxLevel > 1
        } catch (e: Exception) {
            Log.w(TAG, "Flashlight intensity check failed", e)
            false
        }
    }

    /**
     * Get device-specific status bar icon names.
     * Different manufacturers use different icon identifiers.
     */
    fun getStatusBarIconMappings(): Map<String, List<String>> {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        return when {
            manufacturer.contains("samsung") -> mapOf(
                "wifi" to listOf("wifi", "wifi_p2p", "wifi_ap"),
                "mobile_data" to listOf("data_connection", "mobile"),
                "bluetooth" to listOf("bluetooth", "bluetooth_connected"),
                "battery" to listOf("battery", "charging"),
                "alarm" to listOf("alarm_clock"),
                "nfc" to listOf("nfc"),
                "location" to listOf("location", "gps"),
                "hotspot" to listOf("hotspot", "tethering"),
                "vpn" to listOf("vpn", "secure_mode")
            )
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> mapOf(
                "wifi" to listOf("wifi", "wifidisplay"),
                "mobile_data" to listOf("mobile", "data_type"),
                "bluetooth" to listOf("bluetooth"),
                "battery" to listOf("battery"),
                "alarm" to listOf("alarm_clock"),
                "nfc" to listOf("nfc"),
                "location" to listOf("location"),
                "hotspot" to listOf("hotspot"),
                "vpn" to listOf("vpn")
            )
            manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> mapOf(
                "wifi" to listOf("wifi"),
                "mobile_data" to listOf("mobile", "data_type"),
                "bluetooth" to listOf("bluetooth"),
                "battery" to listOf("battery"),
                "alarm" to listOf("alarm_clock"),
                "nfc" to listOf("nfc"),
                "location" to listOf("location"),
                "hotspot" to listOf("hotspot"),
                "vpn" to listOf("vpn")
            )
            else -> mapOf(
                // AOSP/Pixel default
                "wifi" to listOf("wifi"),
                "mobile_data" to listOf("mobile"),
                "bluetooth" to listOf("bluetooth"),
                "battery" to listOf("battery"),
                "alarm" to listOf("alarm_clock"),
                "nfc" to listOf("nfc"),
                "location" to listOf("location"),
                "hotspot" to listOf("hotspot"),
                "vpn" to listOf("vpn")
            )
        }
    }

    /**
     * Get safe secure settings that are known to work on this device
     */
    fun getSafeSecureSettings(context: Context): List<String> {
        val commonSettings = listOf(
            "screen_brightness",
            "screen_brightness_mode",
            "adaptive_sleep",
            "accelerometer_rotation"
        )
        
        return commonSettings.filter { setting ->
            try {
                Settings.Secure.getString(context.contentResolver, setting) != null
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Get device manufacturer and model for logging/debugging
     */
    fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.SDK_INT})"
    }

    /**
     * Check if device is running stock AOSP-based ROM
     */
    fun isAOSPBased(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer.contains("google") || 
               manufacturer.contains("essential") ||
               Build.DISPLAY.contains("lineage", ignoreCase = true)
    }

    /**
     * Check if device supports AOD (Always On Display)
     */
    fun isAODSupported(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, "doze_always_on", -1) != -1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Log feature capability for debugging
     */
    fun logFeatureCapability(feature: String, available: Boolean, details: String = "") {
        val status = if (available) "✓" else "✗"
        Log.d(TAG, "[$status] $feature | ${getDeviceInfo()} | $details")
    }
}
