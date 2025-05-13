package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class TokenResponse(val token: String, val id: String)
