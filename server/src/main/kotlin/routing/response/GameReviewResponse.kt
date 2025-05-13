package com.example.routing.response

import com.example.model.GameSummary
import com.example.model.MoveEvaluation
import kotlinx.serialization.Serializable


@Serializable
data class GameReviewResponse(
    val headers: Map<String, String>,
    val moveEvaluations: List<MoveEvaluation>,
    val summary: GameSummary
)