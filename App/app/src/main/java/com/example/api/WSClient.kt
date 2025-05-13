package com.example.api

import android.util.Log
import com.example.model.MoveData
import com.example.utils.Config
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString

class WebSocketManager(private val id: String?) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun connect(
        onMessageReceived: (String) -> Unit,
        onMoveReceived: (MoveData) -> Unit = {}
    ) {
        val userId = id ?: return
        val request = Request.Builder()
            .url("${Config.WS_URL}/chat/$userId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
                try {
                    val move = json.decodeFromString<MoveData>(text)
                    onMoveReceived(move)
                } catch (e: Exception) {
                    Log.w("WebSocket", "Not a move JSON, fallback to raw: ${e.message}")
                    onMessageReceived(text)
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(1000, null)
                Log.d("WebSocket", "Closing: $code / $reason")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Failure: ${t.message}")
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun sendMove(move: MoveData) {
        val moveJson = json.encodeToString(move)
        webSocket?.send(moveJson)
    }

    fun disconnect() {
        webSocket?.close(1000, null)
    }
}

