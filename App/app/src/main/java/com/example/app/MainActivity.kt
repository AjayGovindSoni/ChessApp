package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.theme.AppTheme
import com.example.model.GameReviewResponse
import com.example.screens.BoardScreen
import com.example.screens.ChatScreen
import com.example.screens.GameAnalysisScreen
import com.example.screens.GameReviewScreen
import com.example.screens.GuestScreen
import com.example.screens.HomeScreen
import com.example.screens.LoginScreen
import com.example.screens.MainMenuScreen
import com.example.screens.OnlineBoardScreen
import com.example.screens.RegistrationScreen
import com.example.screens.RemoteBoardScreen
import com.example.screens.ResetPasswordScreen
import com.example.screens.WelcomeScreen
import com.example.screens.WorkInProgressScreen
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegistrationScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("chat/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            ChatScreen(navController, id)
        }
        composable("guest") { GuestScreen(navController) }
        composable("main_menu") { MainMenuScreen(navController) }
        composable("board") { BoardScreen() }
        composable("remote_board") { RemoteBoardScreen() }
        composable("work_in_progress") { WorkInProgressScreen() }
        composable("game_review/{token}") { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            GameReviewScreen(token = token, navController = navController)
        }
        composable("GameAnalysis/{reviewResult}") { backStackEntry ->
            val reviewJson = backStackEntry.arguments?.getString("reviewResult")
            val review = Gson().fromJson(reviewJson, GameReviewResponse::class.java)
            GameAnalysisScreen(review as GameReviewResponse, navController)
        }
        composable("online_board/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            OnlineBoardScreen(id = id.toString(), navController = navController)
        }

        composable("reset_password") {
            ResetPasswordScreen(navController)
        }
    }
}

