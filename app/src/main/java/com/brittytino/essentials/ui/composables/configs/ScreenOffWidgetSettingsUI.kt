package com.brittytino.essentials.ui.composables.configs

import android.content.SharedPreferences
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import com.brittytino.essentials.R
import com.brittytino.essentials.viewmodels.MainViewModel
import com.brittytino.essentials.ui.components.pickers.HapticFeedbackPicker
import com.brittytino.essentials.ui.components.containers.RoundedCardContainer
import com.brittytino.essentials.ui.modifiers.highlight
import com.brittytino.essentials.domain.HapticFeedbackType
import com.brittytino.essentials.utils.performHapticFeedback

@Composable
fun ScreenOffWidgetSettingsUI(
    viewModel: MainViewModel,
    selectedHaptic: HapticFeedbackType,
    onHapticSelected: (HapticFeedbackType) -> Unit,
    vibrator: Vibrator?,
    prefs: SharedPreferences,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Haptic Feedback Category
        Text(
            text = stringResource(R.string.settings_section_haptic),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RoundedCardContainer(
            modifier = Modifier,
            spacing = 8.dp,
            cornerRadius = 24.dp
        ) {
            HapticFeedbackPicker(
                selectedFeedback = selectedHaptic,
                onFeedbackSelected = { type ->
                    prefs.edit {
                        putString("haptic_feedback_type", type.name)
                    }
                    onHapticSelected(type)
                    viewModel.setHapticFeedback(type, context)
                    if (vibrator != null) {
                        performHapticFeedback(vibrator, type)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "haptic_picker")
            )
        }
    }
}
