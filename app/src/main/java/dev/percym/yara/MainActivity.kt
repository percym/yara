package dev.percym.yara

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.percym.yara.auth.AuthManager
import dev.percym.yara.ui.auth.LoginScreen
import dev.percym.yara.ui.products.ProductsScreen
import dev.percym.yara.ui.theme.YaRATheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YaRATheme {
                YaRAApp()
            }
        }
    }
}

@Composable
private fun YaRAApp() {
    val navController = rememberNavController()
    val authManager = remember { AuthManager(navController.context) }
    val startDest = if (authManager.currentUser != null) "products" else "login"

    // Request location + notification permissions once on the products screen
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions handled silently; geofences require ACCESS_BACKGROUND_LOCATION
           which must be granted separately via system settings on Android 10+ */ }

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = {
                    navController.navigate("products") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("products") {
            LaunchedEffect(Unit) {
                val perms = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                permissionLauncher.launch(perms.toTypedArray())
            }

            ProductsScreen(
                onSignOut = {
                    authManager.signOut()
                    navController.navigate("login") {
                        popUpTo("products") { inclusive = true }
                    }
                }
            )
        }
    }
}
