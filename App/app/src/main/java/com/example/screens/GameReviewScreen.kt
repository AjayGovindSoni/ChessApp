package com.example.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.api.GameReviewRequest
import com.example.api.reviewGame
import com.example.model.GameReviewResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun GameReviewScreen(token: String?, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pgn by remember { mutableStateOf("") }
    var reviewResult by remember { mutableStateOf<GameReviewResponse?>(null) }

    // Check auth and redirect
    LaunchedEffect(Unit) {
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Unauthorized! Redirecting to login", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = pgn,
            onValueChange = { pgn = it },
            label = { Text("Input PGN") },
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val gameReviewRequest = GameReviewRequest(pgn = pgn)
                    val response: GameReviewResponse? = try {
                        reviewGame(
                            gameReviewRequest = gameReviewRequest,
                            token = token ?: ""
                        )
                    } catch (e: Exception) {
                        Log.d("Review Error", "${e}")
                        e.printStackTrace()
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        null
                    }

                    reviewResult = response
                    println("Review: $response")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Review")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reviewResult != null) {
            if (reviewResult!!.moveEvaluations.size > 1) {
                Box(
                    modifier = Modifier
                        .height(350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = "$reviewResult")
                }

                val reviewJson = Uri.encode(Gson().toJson(reviewResult))
                navController.navigate("GameAnalysis/$reviewJson")
            }
        }
    }
}