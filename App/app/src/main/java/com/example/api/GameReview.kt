package com.example.api

import android.util.Log
import com.example.model.GameReviewResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import java.net.URLEncoder
import com.example.utils.Config

suspend fun reviewGame(gameReviewRequest: GameReviewRequest, token: String): GameReviewResponse? {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 300_000  // 60 seconds for full request (optional)
            connectTimeoutMillis = 30_000  // 30 seconds to establish connection
            socketTimeoutMillis = 60_000   // 60 seconds to read data from socket
        }
    }

    val url = "${Config.BASE_URL}/user/gameReview"

    val response = client.post(url) {
        headers {
            append(HttpHeaders.Authorization, "Bearer $token")
        }
        contentType(ContentType.Application.Json)
        setBody(
            gameReviewRequest
        )
    }

    val review = response.body<GameReviewResponse>()
    println(review)

    return if(true) review else null
}

@Serializable
data class GameReviewRequest(
    val pgn: String,
)