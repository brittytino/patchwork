package com.brittytino.patchwork.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brittytino.patchwork.R
import com.brittytino.patchwork.data.database.entity.ActionHistoryEntry
import com.brittytino.patchwork.data.database.entity.ActionType
import com.brittytino.patchwork.viewmodels.ActionHistoryViewModel
import com.brittytino.patchwork.viewmodels.FilterType
import com.brittytino.patchwork.viewmodels.TimeRange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActionHistoryViewModel = viewModel()
) {
    val actions by viewModel.actions.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Action History")
                        Text(
                            "$totalCount total actions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Time range selector
            TimeRangeSelector(
                selected = selectedTimeRange,
                onSelect = { viewModel.setTimeRange(it) }
            )
            
            if (actions.isEmpty()) {
                EmptyHistoryState()
            } else {
                // Group by date
                val groupedActions = actions.groupBy { it.getDateHeader() }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    groupedActions.forEach { (date, actionsForDate) ->
                        item {
                            DateHeader(date)
                        }
                        
                        items(actionsForDate) { action ->
                            ActionHistoryItem(action = action)
                        }
                    }
                }
            }
        }
    }
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will permanently delete all action history. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = TimeRange.entries.indexOf(selected),
        edgePadding = 16.dp
    ) {
        TimeRange.entries.forEach { range ->
            Tab(
                selected = selected == range,
                onClick = { onSelect(range) },
                text = {
                    Text(
                        when (range) {
                            TimeRange.LAST_HOUR -> "Last Hour"
                            TimeRange.TODAY -> "Today"
                            TimeRange.LAST_7_DAYS -> "Last 7 Days"
                            TimeRange.LAST_30_DAYS -> "Last 30 Days"
                            TimeRange.ALL_TIME -> "All Time"
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ActionHistoryItem(action: ActionHistoryEntry) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on action type
            Icon(
                painter = painterResource(action.actionType.getIcon()),
                contentDescription = null,
                tint = if (action.success) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = action.getTimeAgo(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    // Trigger source badge
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = action.triggerSource.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Show error if failed
                if (!action.success && action.errorMessage != null) {
                    Text(
                        text = "Error: ${action.errorMessage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                "No Actions Yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Actions will appear here as you use Patchwork",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Extension functions
fun ActionHistoryEntry.getDateHeader(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    
    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
        
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        
        else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

fun ActionHistoryEntry.getTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

fun ActionType.getIcon(): Int {
    return when (this) {
        ActionType.APP_FROZEN -> R.drawable.rounded_stop_circle_24
        ActionType.APP_UNFROZEN -> R.drawable.rounded_stop_circle_24
        ActionType.VOLUME_CHANGED -> R.drawable.rounded_volume_up_24
        ActionType.BRIGHTNESS_CHANGED -> R.drawable.rounded_brightness_medium_24
        ActionType.NIGHT_LIGHT_TOGGLED -> R.drawable.rounded_nightlight_24
        ActionType.AOD_TOGGLED -> R.drawable.rounded_settings_24
        ActionType.WIFI_TOGGLED -> R.drawable.rounded_wifi_tethering_24
        ActionType.BLUETOOTH_TOGGLED -> R.drawable.rounded_bluetooth_24
        ActionType.AUTOMATION_TRIGGERED -> R.drawable.rounded_settings_24
        ActionType.QS_TILE_TOGGLED -> R.drawable.rounded_settings_24
        ActionType.SYSTEM_SETTING_CHANGED -> R.drawable.rounded_settings_24
        ActionType.APP_BEHAVIOR_APPLIED -> R.drawable.rounded_settings_24
        ActionType.APP_COOLDOWN_BLOCKED -> R.drawable.rounded_stop_circle_24
        ActionType.APP_COOLDOWN_TRIGGERED -> R.drawable.rounded_timer_24
        ActionType.IDLE_APP_ACTION -> R.drawable.rounded_settings_24
        ActionType.SNAPSHOT_SAVED -> R.drawable.rounded_camera_24
        ActionType.SNAPSHOT_RESTORED -> R.drawable.rounded_arrow_back_24
        else -> R.drawable.rounded_info_24
    }
}
