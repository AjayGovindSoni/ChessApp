package com.example.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val email: String,
    val password: String,
)

@Serializable
data class ResetPassword(
    val email: String
)

@Serializable
data class SetNewPassword(
    val email: String,
    val newPassword : String
)