package com.brittytino.essentials.services.tiles

import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import com.brittytino.essentials.R
import com.brittytino.essentials.utils.ShizukuUtils

class MonoAudioTileService : BaseTileService() {

    override fun getTileLabel(): String = "Mono Audio"

    override fun getTileSubtitle(): String {
        return if (isMonoAudioEnabled()) "On" else "Off"
    }

    override fun hasFeaturePermission(): Boolean {
        // Private secure settings can only be modified by ADB, system apps, or
        // apps with a target sdk of Android 5.1 and lower.
        return com.brittytino.essentials.utils.ShellUtils.hasPermission(this) && com.brittytino.essentials.utils.ShellUtils.isAvailable(this)
    }

    override fun getTileIcon(): Icon {
        return Icon.createWithResource(this, R.drawable.rounded_headphones_24)
    }

    override fun getTileState(): Int {
        return if (isMonoAudioEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }

    override fun onTileClick() {
        val newState = if (isMonoAudioEnabled()) 0 else 1
        com.brittytino.essentials.utils.ShellUtils.runCommand(this, "settings put system master_mono $newState")
    }

    private fun isMonoAudioEnabled(): Boolean {
        return Settings.System.getInt(contentResolver, "master_mono", 0) == 1
    }
}
