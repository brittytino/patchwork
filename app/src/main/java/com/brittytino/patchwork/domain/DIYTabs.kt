package com.brittytino.patchwork.domain

import androidx.annotation.StringRes
import com.brittytino.patchwork.R

enum class DIYTabs(@StringRes val title: Int, val subtitle: Any, val iconRes: Int) {
    PATCHWORK(R.string.tab_essentials, if (com.brittytino.patchwork.BuildConfig.DEBUG) "" else "=^..^=", R.drawable.ic_stat_name),
    FREEZE(R.string.tab_freeze, R.string.tab_freeze_subtitle, R.drawable.rounded_mode_cool_24),
    DIY(R.string.tab_diy, R.string.tab_diy_subtitle, R.drawable.rounded_experiment_24),
    APPS(R.string.tab_apps, R.string.tab_apps_subtitle, R.drawable.rounded_apps_24)
}