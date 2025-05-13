package com.example.model

import kotlinx.serialization.Serializable


enum class MoveCategory {
    BRILLIANT,
    GREAT,
    EXCELLENT,
    BEST,
    GOOD,
    BOOK,
    INACCURACY,
    MISTAKE,
    MISS,
    BLUNDER
}


@Serializable
data class GameReviewResponse(
    val headers: Map<String, String>,
    val moveEvaluations: List<MoveEvaluation>,
    val summary: GameSummary
)

data class GameHeaders(
    val Event: String,
    val Site: String,
    val Date: String,
    val Round: String,
    val White: String,
    val Black: String,
    val Result: String
)


@Serializable
data class MoveEvaluation(
    val moveNumber: Int,
    val playerToMove: String,
    val move: String,
    val evaluation: Double,
    val bestMove: String,
    val category: MoveCategory,
    val comment: String
)


@Serializable
data class GameSummary(
    val result: String,
    val whiteBrilliant: Int,
    val whiteGreat: Int,
    val whiteExcellent: Int,
    val whiteBest: Int,
    val whiteGood: Int,
    val whiteBook: Int,
    val whiteInaccuracies: Int,
    val whiteMistakes: Int,
    val whiteMisses: Int,
    val whiteBlunders: Int,
    val blackBrilliant: Int,
    val blackGreat: Int,
    val blackExcellent: Int,
    val blackBest: Int,
    val blackGood: Int,
    val blackBook: Int,
    val blackInaccuracies: Int,
    val blackMistakes: Int,
    val blackMisses: Int,
    val blackBlunders: Int
)

