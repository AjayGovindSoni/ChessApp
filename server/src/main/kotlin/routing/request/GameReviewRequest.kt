package com.example.routing.request

import kotlinx.serialization.Serializable

@Serializable
data class GameReviewRequest(
    val pgn: String,
)