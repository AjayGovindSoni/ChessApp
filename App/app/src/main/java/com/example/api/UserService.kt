package com.example.api

import com.example.models.User
import com.example.utils.Config
import com.example.utils.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun getUser(): List<User>? {
    if (TokenManager.authToken == null) {
        println("Error: No authentication token available.")
        return null
    }

    val response: HttpResponse = client.get("${Config.BASE_URL}/user") {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${TokenManager.authToken}")
        }
    }

    return if (response.status == HttpStatusCode.OK) {
        response.body()
    } else {
        println("Failed to fetch user: ${response.status}")
        null
    }
}

suspend fun getUserById(userId: String): User? {
    if (TokenManager.authToken == null) {
        println("Error: No authentication token available.")
        return null
    }

    val response: HttpResponse = client.get("${Config.BASE_URL}/user/$userId") {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${TokenManager.authToken}")
        }
    }

    return if (response.status == HttpStatusCode.OK) {
        response.body()
    } else {
        println("Failed to fetch user by ID: ${response.status}")
        null
    }
}
