package com.guardian.launcher.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.guardian.launcher.ui.theme.GuardianTheme

/**
 * Main Launcher Activity.
 * 
 * This activity serves as the default home screen.
 * It displays the child mode interface by default.
 * 
 * Parent mode is accessed through a separate protected activity.
 */
class LauncherActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots in child mode
        // window.setFlags(
        //     WindowManager.LayoutParams.FLAG_SECURE,
        //     WindowManager.LayoutParams.FLAG_SECURE
        // )
        
        enableEdgeToEdge()
        
        setContent {
            GuardianTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // Disable back button in child mode
        // Do nothing
    }
    
    @Deprecated("Deprecated in Java")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Prevent task switching in child mode
        // TODO: Bring launcher back to front if child mode is active
    }
}
