package com.brittytino.essentials.ui.composables.configs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.brittytino.essentials.R
import com.brittytino.essentials.ui.components.cards.FeatureCard
import com.brittytino.essentials.ui.components.cards.IconToggleItem
import com.brittytino.essentials.ui.components.cards.PermissionCard
import com.brittytino.essentials.ui.components.sheets.AppSelectionSheet
import com.brittytino.essentials.ui.components.containers.RoundedCardContainer
import com.brittytino.essentials.viewmodels.MainViewModel
import com.brittytino.essentials.utils.PermissionUtils
import com.brittytino.essentials.utils.BiometricHelper
import androidx.fragment.app.FragmentActivity
import com.brittytino.essentials.ui.modifiers.highlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsUI(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    highlightKey: String? = null
) {
    val context = LocalContext.current
    var isAppSelectionSheetOpen by remember { mutableStateOf(false) }
    
    val isAppLockEnabled by viewModel.isAppLockEnabled
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_section_security),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RoundedCardContainer(
            modifier = Modifier,
            spacing = 2.dp,
            cornerRadius = 24.dp
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_shield_lock_24,
                title = stringResource(R.string.app_lock_enable_title),
                isChecked = isAppLockEnabled,
                onCheckedChange = { enabled ->
                    if (context is FragmentActivity) {
                        BiometricHelper.showBiometricPrompt(
                            activity = context,
                            title = context.getString(R.string.app_lock_auth_title),
                            subtitle = if (enabled) context.getString(R.string.app_lock_enable_auth_subtitle) else context.getString(R.string.app_lock_disable_auth_subtitle),
                            onSuccess = { viewModel.setAppLockEnabled(enabled, context) }
                        )
                    } else {
                        viewModel.setAppLockEnabled(enabled, context)
                    }
                },
                enabled = isAccessibilityEnabled,
                onDisabledClick = {},
                modifier = Modifier.highlight(highlightKey == "app_lock_enabled")
            )

            FeatureCard(
                title = stringResource(R.string.app_lock_select_apps_title),
                description = stringResource(R.string.app_lock_select_apps_desc),
                iconRes = R.drawable.rounded_app_registration_24,
                isEnabled = isAppLockEnabled,
                showToggle = false,
                hasMoreSettings = true,
                onToggle = {},
                onClick = { isAppSelectionSheetOpen = true },
                modifier = Modifier.highlight(highlightKey == "app_lock_selected_apps")
            )
        }

        Text(
            text = stringResource(R.string.app_lock_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.app_lock_warning),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stringResource(R.string.app_lock_biometric_note),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isAppSelectionSheetOpen) {
            AppSelectionSheet(
                onDismissRequest = { isAppSelectionSheetOpen = false },
                onLoadApps = { viewModel.loadAppLockSelectedApps(it) },
                onSaveApps = { ctx, apps -> viewModel.saveAppLockSelectedApps(ctx, apps) },
                onAppToggle = { ctx, pkg, enabled -> viewModel.updateAppLockAppEnabled(ctx, pkg, enabled) }
            )
        }
    }
}
