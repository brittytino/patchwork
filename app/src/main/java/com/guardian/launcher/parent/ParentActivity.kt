package com.guardian.launcher.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guardian.launcher.ui.theme.GuardianTheme

/**
 * Parent Activity.
 * 
 * Protected access for parents to:
 * - Configure approved apps
 * - Set time limits
 * - Manage rules
 * - View usage reports
 * 
 * Access requires:
 * - PIN/Password authentication
 * - or Biometric authentication
 */
class ParentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Verify parent authentication
        // TODO: If not authenticated, show auth screen
        
        enableEdgeToEdge()
        
        setContent {
            GuardianTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ParentDashboard()
                }
            }
        }
    }
}

@Composable
private fun ParentDashboard() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Mode") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Parent Controls",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // TODO: Add sections for:
            // - App management
            // - Time rules
            // - Usage statistics
            // - Security settings
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configure your child's device settings here.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
