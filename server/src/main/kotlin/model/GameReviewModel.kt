package com.example.model

import kotlinx.serialization.Serializable
import java.io.BufferedReader
import java.io.OutputStreamWriter


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

data class ChessPosition(val fen: String, private val stockfishInput: OutputStreamWriter, private val stockfishOutput: BufferedReader) {

    fun makeMove(move: String): ChessPosition {
        sendCommand("position fen $fen moves $move")
        sendCommand("d")

        var newFen: String? = null
        while (true) {
            val line = stockfishOutput.readLine() ?: break
            println(line) // Debugging output

            if (line.startsWith("Fen:") || line.startsWith("FEN:")) {
                newFen = line.split("Fen: ")[1].trim()
                break
            }
        }

        return ChessPosition(newFen ?: fen, stockfishInput, stockfishOutput) // Return updated position
    }


    private fun sendCommand(command: String) {
        stockfishInput.write("$command\n")
        stockfishInput.flush()
    }
}


data class PositionAnalysis(
    val score: Double,
    val bestMove: String
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

data class ParsedGame(
    val header: Map<String, String>,
    val moves: List<String>
)