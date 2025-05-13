package com.example.api

import com.example.models.LoginRequest
import com.example.models.TokenResponse
import com.example.utils.Config
import com.example.utils.TokenManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun login(email: String, password: String): Boolean {
    val response: HttpResponse = client.post("${Config.BASE_URL}/auth") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(email, password))
    }

    return if (response.status == HttpStatusCode.OK) {
        val tokenResponse: TokenResponse = response.body()
        TokenManager.authToken = tokenResponse.token
        TokenManager.id = tokenResponse.id
        println("Login Successful! Token: ${TokenManager.authToken}")
        println("User ID: ${TokenManager.id}")
        true
    } else {
        println("Login Failed: ${response.status}")
        false
    }
}

suspend fun registerUser(email: String, password: String): Boolean {
    val response: HttpResponse = client.post("${Config.BASE_URL}/user") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(email, password))
    }
    println("Register Response: ${response.status}")
    return response.status == HttpStatusCode.OK
}

suspend fun verifyOtp(otp: String): Boolean {
    val response: HttpResponse = client.post("${Config.BASE_URL}/user/otp/$otp")
    println("OTP Verification Response: ${response.status}")
    return response.status == HttpStatusCode.Created
}


suspend fun sendResetOtp(email: String): Boolean {
    val response = client.post("${Config.BASE_URL}/user/reset-password-request") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("email" to email))
    }
    return response.status == HttpStatusCode.OK
}

suspend fun verifyResetOtp(email: String, otp: String): Boolean {
    val response = client.post("${Config.BASE_URL}/user/verify-reset-otp/$email/$otp")
    return response.status == HttpStatusCode.OK
}

suspend fun resetPassword(email: String, newPassword: String): Boolean {
    val response = client.post("${Config.BASE_URL}/user/reset-password") {
        contentType(ContentType.Application.Json)
        setBody(mapOf("email" to email, "newPassword" to newPassword))
    }
    return response.status == HttpStatusCode.OK
}
