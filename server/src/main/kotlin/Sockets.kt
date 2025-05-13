package com.example

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Serializable
data class ChatMessage(val sender: String, val content: String)

fun Application.configureSockets() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {

        val communitySessions =
            Collections.synchronizedList<WebSocketServerSession>(ArrayList())
//
        val communityUserSessions = Collections.synchronizedMap(mutableMapOf<String, WebSocketServerSession>())
//        val userPairs = Collections.synchronizedMap(mutableMapOf<String, String>())
//
        webSocket("/api/community"){
            communitySessions.add(this)

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val receivedMessage = frame.readText()
                        println(receivedMessage)

                        for (session in communitySessions) {
                            session.send(receivedMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket Error: ${e.localizedMessage}")
            } finally {
                communitySessions.remove(this)
                close(CloseReason(CloseReason.Codes.NORMAL, "User Disconnected"))
            }
        }
//
//        webSocket("/api/chat/{userId}") {
//            val userId = call.parameters["userId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing user ID"))
//
//            userSessions[userId] = this
//
//            try {
//                incoming.consumeEach { frame ->
//                    if (frame is Frame.Text) {
//                        val receivedMessage = frame.readText()
//                        println("User $userId sent: $receivedMessage")
//
//                        val pairedUserId = userPairs[userId]
//                        val pairedSession = userSessions[pairedUserId]
//
//                        if (pairedSession != null) {
//                            pairedSession.send(receivedMessage)
//                        } else {
//                            send("No paired user found")
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                println("WebSocket Error for $userId: ${e.localizedMessage}")
//            } finally {
//                userSessions.remove(userId)
//                userPairs.remove(userId)
//                userPairs.entries.removeIf { it.value == userId } // Remove any pair referencing this user
//                close(CloseReason(CloseReason.Codes.NORMAL, "User Disconnected"))
//            }
//        }

        val userSessions = Collections.synchronizedMap(mutableMapOf<String, WebSocketServerSession>())
        val userPairs = Collections.synchronizedMap(mutableMapOf<String, String>())
        val waitingQueue = Collections.synchronizedList(LinkedList<String>())

        webSocket("/api/chat/{userId}") {
            val userId = call.parameters["userId"]
            if (userId == null) {
                close(
                    CloseReason(
                        CloseReason.Codes.VIOLATED_POLICY,
                        "Missing user ID"
                    )
                )
                return@webSocket
            }

            userSessions[userId] = this
            println("User connected: $userId")
            var whitePlayer: String? = null
            var blackPlayer: String? = null

            var pairedUser: String? = null
            synchronized(waitingQueue) {
                if (waitingQueue.contains(userId)) {
                    println("Duplicate connection for user $userId - already waiting")
                    return@webSocket // Or you can choose to just not pair again
                }
                if (waitingQueue.isNotEmpty()) {
                    val opponentId = waitingQueue.removeFirst()
                    userPairs[userId] = opponentId
                    userPairs[opponentId] = userId
                    pairedUser = opponentId
                    println("Paired $userId with $opponentId")

                    val assignWhite = listOf(userId, opponentId).shuffled()
                    whitePlayer = assignWhite[0]
                    blackPlayer = assignWhite[1]
                } else {
                    waitingQueue.add(userId)
                    println("User $userId added to waiting queue")
                }
            }

            whitePlayer?.let {
                userSessions[it]?.send("""{ "type": "assign_color", "color": "White" }""")
            }
            blackPlayer?.let {
                userSessions[it]?.send("""{ "type": "assign_color", "color": "Black" }""")
            }
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val message = frame.readText()
                        println("User $userId sent: $message")

                        val opponentId = userPairs[userId]
                        val opponentSession = userSessions[opponentId]

                        if (opponentSession != null) {
                            opponentSession.send(message)
                        } else {
                            send("Waiting for a partner...")
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket error for $userId: ${e.localizedMessage}")
            } finally {
                println("User disconnected: $userId")

                userSessions.remove(userId)
                userPairs.remove(userId)
                userPairs.entries.removeIf { it.value == userId }
                waitingQueue.remove(userId)

                close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected"))
            }
        }
    }
}
