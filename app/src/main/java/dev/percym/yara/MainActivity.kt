package dev.percym.yara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dev.percym.yara.auth.AuthManager
import dev.percym.yara.service.ShoppingReminderService
import dev.percym.yara.ui.auth.LoginScreen
import dev.percym.yara.ui.products.ProductsScreen
import dev.percym.yara.ui.theme.YaRATheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (FirebaseAuth.getInstance().currentUser != null) {
            ShoppingReminderService.start(this)
        }
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

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = {
                    ShoppingReminderService.start(navController.context)
                    navController.navigate("products") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("products") {
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
