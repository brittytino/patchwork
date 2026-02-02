package com.brittytino.essentials.services.automation.executors

import android.content.Context
import com.brittytino.essentials.domain.diy.Action

interface ActionExecutor {
    suspend fun execute(context: Context, action: Action)
}
