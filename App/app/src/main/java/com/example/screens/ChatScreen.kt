package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.api.CommunityWebSocketManager

//@Composable
//fun ChatScreen(navController: NavController, id: String?) {
//    val context = LocalContext.current
//    var message by remember { mutableStateOf("") }
//    var receivedMessages by remember { mutableStateOf(listOf<String>()) }
//    val webSocketManager = remember { CommunityWebSocketManager(id) }
//
//    LaunchedEffect(Unit) {
//        if (id.isNullOrEmpty()) {
//            Toast.makeText(context, "Unauthorized! Redirecting to login", Toast.LENGTH_SHORT).show()
//            navController.navigate("login")
////        } else {
////            CommunityWebSocketManager.connect { newMessage ->
////                receivedMessages = receivedMessages + newMessage
////            }
//        }
//    }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Text(text = "WebSocket Chat", style = MaterialTheme.typography.bodyLarge)
//
//        // Chat Messages
//        receivedMessages.forEach { msg ->
//            Text(text = msg, modifier = Modifier.padding(4.dp))
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Input Field
//        TextField(
//            value = message,
//            onValueChange = { message = it },
//            label = { Text("Enter message") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Send Button
//        Button(
//            onClick = {
//                if (message.isNotEmpty()) {
//                    webSocketManager.sendMessage(message)
//                    message = "" // Clear input
//                }
//            },
//            enabled = id != null
//        ) {
//            Text("Send Message")
//        }
//    }
//}

@Composable
fun ChatScreen(navController: NavController, id: String?) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var receivedMessages by remember { mutableStateOf(listOf<String>()) }

    // Retain WS manager across recompositions
    val webSocketManager = remember { CommunityWebSocketManager(id) }

    // WebSocket lifecycle: connect when composable enters composition
    LaunchedEffect(id) {
        if (id.isNullOrEmpty()) {
            Toast.makeText(context, "Unauthorized! Redirecting to login", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
        } else {
            webSocketManager.connect(
                onMessageReceived = { newMessage ->
                    receivedMessages = receivedMessages + newMessage
                }
            )
        }
    }

    // Clean up when ChatScreen is removed from composition
    DisposableEffect(Unit) {
        onDispose {
            webSocketManager.disconnect()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = "WebSocket Chat", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Chat messages
        Column(modifier = Modifier.weight(1f)) {
            receivedMessages.forEach { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(text = msg, modifier = Modifier.padding(4.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message input
        TextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Enter message") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Send button
        Button(
            onClick = {
                if (message.isNotBlank()) {
                    webSocketManager.sendMessage(message)
                    message = ""
                }
            },
            enabled = id != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Message")
        }
    }
}
