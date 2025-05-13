package com.example.model

import java.util.UUID

data class User (
    val id: UUID,
    val email: String,
    val password: String,
    )

data class otp_requests(
    val otp: String,
    val email: String
)