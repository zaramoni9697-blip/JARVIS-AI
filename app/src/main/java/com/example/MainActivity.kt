package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ApiSetupScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignupScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.JarvisAITheme
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.VoiceActionResult

class MainActivity : ComponentActivity() {
  private val viewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val settings by viewModel.settings.collectAsState()

      JarvisAITheme(
        themeMode = settings.themeMode,
        isDarkTheme = settings.isDarkTheme
      ) {
        Scaffold(
          contentWindowInsets = WindowInsets.safeDrawing,
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            JarvisNavApp(viewModel = viewModel)
          }
        }
      }
    }
  }
}

@Composable
fun JarvisNavApp(viewModel: JarvisViewModel) {
  val navController = rememberNavController()
  val currentUser by viewModel.currentUser.collectAsState()
  val isAuthChecking by viewModel.isAuthChecking.collectAsState()

  // Listen for global voice action events that require navigation
  LaunchedEffect(navController) {
    viewModel.voiceActionEvent.collect { result ->
      if (result is VoiceActionResult.Navigated) {
        val dest = when (result.destination) {
          "vision" -> "vision"
          "chat" -> "chat"
          "memory" -> "memory"
          "voice" -> "voice"
          else -> "home"
        }
        navController.navigate(dest) {
          launchSingleTop = true
        }
      }
    }
  }

  NavHost(
    navController = navController,
    startDestination = "splash"
  ) {
    composable("splash") {
      SplashScreen(
        onBootComplete = {
          if (currentUser != null) {
            if (currentUser!!.encryptedApiKey.isNullOrBlank()) {
              navController.navigate("api_setup") {
                popUpTo("splash") { inclusive = true }
              }
            } else {
              navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
              }
            }
          } else {
            navController.navigate("login") {
              popUpTo("splash") { inclusive = true }
            }
          }
        }
      )
    }

    composable("login") {
      LoginScreen(
        viewModel = viewModel,
        onLoginSuccess = { hasApiKey ->
          if (hasApiKey) {
            navController.navigate("home") {
              popUpTo("login") { inclusive = true }
            }
          } else {
            navController.navigate("api_setup") {
              popUpTo("login") { inclusive = true }
            }
          }
        },
        onNavigateToSignup = {
          navController.navigate("signup")
        }
      )
    }

    composable("signup") {
      SignupScreen(
        viewModel = viewModel,
        onSignupSuccess = {
          navController.navigate("api_setup") {
            popUpTo("signup") { inclusive = true }
          }
        },
        onNavigateToLogin = {
          navController.popBackStack()
        }
      )
    }

    composable("api_setup") {
      ApiSetupScreen(
        viewModel = viewModel,
        onSetupComplete = {
          navController.navigate("home") {
            popUpTo("api_setup") { inclusive = true }
            popUpTo("login") { inclusive = true }
          }
        },
        onNavigateBack = if (navController.previousBackStackEntry != null) {
          { navController.popBackStack() }
        } else null
      )
    }

    composable("home") {
      HomeScreen(
        viewModel = viewModel,
        onNavigate = { destination ->
          navController.navigate(destination)
        }
      )
    }

    composable("chat") {
      ChatScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    composable("voice") {
      VoiceScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateTo = { dest ->
          navController.navigate(dest)
        }
      )
    }

    composable("vision") {
      VisionScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    composable("history") {
      HistoryScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onOpenSession = {
          navController.navigate("chat")
        }
      )
    }

    composable("memory") {
      MemoryScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    composable("settings") {
      SettingsScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToApiSetup = {
          navController.navigate("api_setup")
        },
        onLogout = {
          navController.navigate("login") {
            popUpTo("home") { inclusive = true }
          }
        }
      )
    }
  }
}
