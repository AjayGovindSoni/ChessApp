import com.example.routing.response.GameReviewResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import com.example.model.MoveCategory
import com.example.model.ChessPosition
import com.example.model.MoveEvaluation
import com.example.model.PositionAnalysis
import com.example.model.GameSummary
import com.example.model.ParsedGame

class ChessGameReviewer(
    private val stockfishPath: String,
    private val analysisDepth: Int = 18
) {
    private val process: Process
    private val input: OutputStreamWriter
    private val output: BufferedReader

    init {
        process = ProcessBuilder(stockfishPath).start()
        input = OutputStreamWriter(process.outputStream)
        output = BufferedReader(InputStreamReader(process.inputStream))

        sendCommand("uci")
        sendCommand("isready")
        readUntil("readyok")
    }

    fun reviewGame(pgn: String): GameReviewResponse {
        val parsedGame = parsePgn(pgn)
        var moves = parsedGame.moves
        println(moves)

        moves = convertSanToUci(moves)

        println("uci moves : ${moves}")

        val evaluations = mutableListOf<MoveEvaluation>()
        var fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        var position = ChessPosition(fen, stockfishInput = input, stockfishOutput = output)
        var previousEvaluation = 0.0
        var previousPosition: ChessPosition? = null

        for ((index, moveStr) in moves.withIndex()) {
            val move = moveStr.trim()
            if (move.matches(Regex("\\d+\\."))) continue

            val evaluation = analyzePosition(position, depth = analysisDepth)

            val isTactical = previousPosition?.let { hasTacticalMotif(it, move) } ?: false
            val isComplexPosition = isComplexPosition(position)

            val moveCategory = if (index > 0) {
                val evalChange = if (index % 2 == 0) {
                    evaluation.score - previousEvaluation
                } else {
                    previousEvaluation - evaluation.score
                }
                categorizeMoveByEvalChange(evalChange, evaluation.bestMove == move, isTactical, isComplexPosition)
            } else {
                MoveCategory.BOOK
            }

            previousPosition = position

            position = position.makeMove(move)
            previousEvaluation = evaluation.score

            val playerToMove = if (index % 2 == 0) "White" else "Black"
            evaluations.add(
                MoveEvaluation(
                    moveNumber = (index / 2) + 1,
                    playerToMove = playerToMove,
                    move = move,
                    evaluation = evaluation.score,
                    bestMove = evaluation.bestMove,
                    category = moveCategory,
                    comment = generateComment(evaluation.score, evaluation.bestMove, move, moveCategory)
                )
            )
        }

        val finalEvaluation = analyzePosition(position, depth = analysisDepth)

        return GameReviewResponse(
            parsedGame.header,
            evaluations,
            generateGameSummary(evaluations)
        )
    }


    private fun hasTacticalMotif(position: ChessPosition, move: String): Boolean {
        return move.contains("x") || move.contains("+") || move.contains("#")
    }

    private fun isComplexPosition(position: ChessPosition): Boolean {
        return false
    }


    private fun categorizeMoveByEvalChange(
        evalChange: Double,
        isBestMove: Boolean,
        isTactical: Boolean,
        isComplex: Boolean
    ): MoveCategory {
        return when {
            evalChange > 1.5 && isTactical && isComplex -> MoveCategory.BRILLIANT

            evalChange > 1.0 -> MoveCategory.GREAT

            evalChange > 0.5 -> MoveCategory.EXCELLENT

            isBestMove -> MoveCategory.BEST

            evalChange > -0.2 -> MoveCategory.GOOD

            evalChange > -0.5 -> MoveCategory.INACCURACY

            isTactical && evalChange <= -0.5 -> MoveCategory.MISS

            evalChange > -1.5 -> MoveCategory.MISTAKE

            else -> MoveCategory.BLUNDER
        }
    }

    private fun analyzePosition(position: ChessPosition, depth: Int): PositionAnalysis {
        sendCommand("position fen ${position.fen}")
        sendCommand("go depth $depth")

        var bestMove = ""
        var score = 0.0

        while (true) {
            val line = output.readLine() ?: continue

            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ")[1]
                break
            } else if (line.contains("score cp")) {
                val cpScorePattern = "score cp (-?\\d+)".toRegex()
                val cpScoreMatch = cpScorePattern.find(line)
                cpScoreMatch?.let {
                    score = it.groupValues[1].toDouble() / 100.0
                }
            } else if (line.contains("score mate")) {
                val mateScorePattern = "score mate (-?\\d+)".toRegex()
                val mateScoreMatch = mateScorePattern.find(line)
                mateScoreMatch?.let {
                    val mateIn = it.groupValues[1].toInt()
                    score = if (mateIn > 0) 999.0 else -999.0
                }
            }
        }

        return PositionAnalysis(score, bestMove)
    }

    private fun generateComment(
        evaluation: Double,
        bestMove: String,
        actualMove: String,
        category: MoveCategory
    ): String {
        val positionComment = when {
            abs(evaluation) < 0.3 -> "Equal position"
            abs(evaluation) < 0.7 -> "Slightly ${if (evaluation > 0) "better for White" else "better for Black"}"
            abs(evaluation) < 1.5 -> "Clear advantage for ${if (evaluation > 0) "White" else "Black"}"
            abs(evaluation) < 3.0 -> "Winning advantage for ${if (evaluation > 0) "White" else "Black"}"
            abs(evaluation) < 10.0 -> "Decisive advantage for ${if (evaluation > 0) "White" else "Black"}"
            else -> if (evaluation > 0) "White has mate" else "Black has mate"
        }

        val categoryComment = when (category) {
            MoveCategory.BRILLIANT -> "Brilliant move!"
            MoveCategory.GREAT -> "Great move!"
            MoveCategory.EXCELLENT -> "Excellent move"
            MoveCategory.BEST -> "Best move"
            MoveCategory.GOOD -> "Good move"
            MoveCategory.BOOK -> "Book move"
            MoveCategory.INACCURACY -> "Inaccuracy"
            MoveCategory.MISTAKE -> "Mistake"
            MoveCategory.MISS -> "Missed opportunity"
            MoveCategory.BLUNDER -> "Blunder"
        }

        val moveComment = if (bestMove != actualMove.replace(Regex("[+#]"), "") &&
            category !in listOf(MoveCategory.BEST, MoveCategory.BRILLIANT, MoveCategory.GREAT, MoveCategory.EXCELLENT)
        ) {
            "Better was $bestMove"
        } else {
            ""
        }

        return "$categoryComment. $positionComment${if (moveComment.isNotEmpty()) ". $moveComment" else ""}"
    }

    private fun generateGameSummary(evaluations: List<MoveEvaluation>): GameSummary {
        var whiteBrilliant = 0
        var whiteGreat = 0
        var whiteExcellent = 0
        var whiteBest = 0
        var whiteGood = 0
        var whiteBook = 0
        var whiteInaccuracies = 0
        var whiteMistakes = 0
        var whiteMisses = 0
        var whiteBlunders = 0

        var blackBrilliant = 0
        var blackGreat = 0
        var blackExcellent = 0
        var blackBest = 0
        var blackGood = 0
        var blackBook = 0
        var blackInaccuracies = 0
        var blackMistakes = 0
        var blackMisses = 0
        var blackBlunders = 0

        for (eval in evaluations) {
            when {
                eval.playerToMove == "White" && eval.category == MoveCategory.BRILLIANT -> whiteBrilliant++
                eval.playerToMove == "White" && eval.category == MoveCategory.GREAT -> whiteGreat++
                eval.playerToMove == "White" && eval.category == MoveCategory.EXCELLENT -> whiteExcellent++
                eval.playerToMove == "White" && eval.category == MoveCategory.BEST -> whiteBest++
                eval.playerToMove == "White" && eval.category == MoveCategory.GOOD -> whiteGood++
                eval.playerToMove == "White" && eval.category == MoveCategory.BOOK -> whiteBook++
                eval.playerToMove == "White" && eval.category == MoveCategory.INACCURACY -> whiteInaccuracies++
                eval.playerToMove == "White" && eval.category == MoveCategory.MISTAKE -> whiteMistakes++
                eval.playerToMove == "White" && eval.category == MoveCategory.MISS -> whiteMisses++
                eval.playerToMove == "White" && eval.category == MoveCategory.BLUNDER -> whiteBlunders++

                eval.playerToMove == "Black" && eval.category == MoveCategory.BRILLIANT -> blackBrilliant++
                eval.playerToMove == "Black" && eval.category == MoveCategory.GREAT -> blackGreat++
                eval.playerToMove == "Black" && eval.category == MoveCategory.EXCELLENT -> blackExcellent++
                eval.playerToMove == "Black" && eval.category == MoveCategory.BEST -> blackBest++
                eval.playerToMove == "Black" && eval.category == MoveCategory.GOOD -> blackGood++
                eval.playerToMove == "Black" && eval.category == MoveCategory.BOOK -> blackBook++
                eval.playerToMove == "Black" && eval.category == MoveCategory.INACCURACY -> blackInaccuracies++
                eval.playerToMove == "Black" && eval.category == MoveCategory.MISTAKE -> blackMistakes++
                eval.playerToMove == "Black" && eval.category == MoveCategory.MISS -> blackMisses++
                eval.playerToMove == "Black" && eval.category == MoveCategory.BLUNDER -> blackBlunders++
            }
        }

        val finalEval = evaluations.lastOrNull()?.evaluation ?: 0.0
        val result = when {
            abs(finalEval) < 0.5 -> "Draw"
            finalEval > 0 -> "White is winning"
            else -> "Black is winning"
        }

        return GameSummary(
            result = result,
            whiteBrilliant = whiteBrilliant,
            whiteGreat = whiteGreat,
            whiteExcellent = whiteExcellent,
            whiteBest = whiteBest,
            whiteGood = whiteGood,
            whiteBook = whiteBook,
            whiteInaccuracies = whiteInaccuracies,
            whiteMistakes = whiteMistakes,
            whiteMisses = whiteMisses,
            whiteBlunders = whiteBlunders,
            blackBrilliant = blackBrilliant,
            blackGreat = blackGreat,
            blackExcellent = blackExcellent,
            blackBest = blackBest,
            blackGood = blackGood,
            blackBook = blackBook,
            blackInaccuracies = blackInaccuracies,
            blackMistakes = blackMistakes,
            blackMisses = blackMisses,
            blackBlunders = blackBlunders
        )
    }

    private fun parsePgn(pgn: String): ParsedGame {
        val headers = mutableMapOf<String, String>()
        val headerRegex = Regex("\\[(\\w+)\\s+\"(.*)\"\\]")
        val moveLines = mutableListOf<String>()

        pgn.lines().forEach { line ->
            val headerMatch = headerRegex.matchEntire(line.trim())
            if (headerMatch != null) {
                val (key, value) = headerMatch.destructured
                headers[key] = value
            } else if (line.trim().isNotEmpty()) {
                moveLines.add(line.trim())
            }
        }

        val moveText = moveLines.joinToString(" ")
            .replace("\\{[^}]*\\}".toRegex(), "")
            .replace("\\([^)]*\\)".toRegex(), "")
            .replace("\\$\\d+".toRegex(), "")
            .replace("\\b\\d+\\.".toRegex(), "")

        val moves = moveText.split(Regex("\\s+"))
            .filter { it.isNotEmpty() }

        return ParsedGame(headers, moves)
    }

    private fun sendCommand(command: String) {
        input.write("$command\n")
        input.flush()
    }

    private fun readUntil(marker: String): List<String> {
        val lines = mutableListOf<String>()
        while (true) {
            val line = output.readLine() ?: break
            lines.add(line)
            if (line.contains(marker)) break
        }
        return lines
    }

    fun close() {
        sendCommand("quit")
        try {
            process.waitFor(1, TimeUnit.SECONDS)
        } finally {
            process.destroyForcibly()
        }
    }
}


fun convertSanToUci(pgnMoves: List<String>): List<String> {
    val pythonScriptPath = "E:\\kotlin\\KTOR\\server\\src\\main\\kotlin\\service\\f.py" // Update with actual path

    // Prepare process to call Python script
    val processBuilder = ProcessBuilder(
        listOf("python", pythonScriptPath) + pgnMoves // Pass moves as arguments
    )

    return try {
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        val uciMoves = reader.readLine()?.split(" ") ?: emptyList()
        val errors = errorReader.readText()

        if (errors.isNotEmpty()) {
            println("Python Error: $errors")
        }

        uciMoves
    } catch (e: Exception) {
        println("Error running Python script: ${e.message}")
        emptyList()
    }
}


//fun main() {
//    val stockfishPath = "E:\\kotlin\\KTOR\\server\\src\\main\\kotlin\\service\\stockfish.exe"
//
//    try {
//        val reviewer = ChessGameReviewer(stockfishPath)
//
//        val samplePgn = """
//            [Event "Example Game"]
//            [Site "Chess.com"]
//            [Date "2023.01.15"]
//            [Round "1"]
//            [White "Player1"]
//            [Black "Player2"]
//            [Result "1-0"]
//
//            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Na5 10. Bc2 c5 11. d4 Qc7 12. Nbd2 cxd4 13. cxd4 exd4 14. Nxd4 Nc6 15. Nxc6 Qxc6 16. e5 dxe5 17. Rxe5 Bd6 18. Re1 Bd7 19. Nf3 Rae8 20. Rxe8 Rxe8 21. Bg5 Be6 22. Qd4 h6 23. Bxf6 gxf6 24. Qxf6 Bh2+ 25. Kh1 Bf4 26. g3 Be5 27. Qf5 Bxg3 28. fxg3 Qxc2 29. Qxe6 Qc4 30. Re1 1-0
//        """.trimIndent()
//
//        val review = reviewer.reviewGame(samplePgn)
//
//        println("Game: ${review.headers["White"]} vs ${review.headers["Black"]}")
//        println("==================================")
//
//        review.moveEvaluations.forEach { eval ->
//            if (eval.playerToMove == "White") {
//                print("${eval.moveNumber}. ")
//            }
//            print("${eval.move} ")
//            println("(${eval.evaluation}) [${eval.category}] - ${eval.comment}")
//        }
//
//        println("\nGame Summary:")
//        println("==================================")
//        println("Result: ${review.summary.result}")
//        println("\nWhite moves:")
//        println("Brilliant: ${review.summary.whiteBrilliant}")
//        println("Great: ${review.summary.whiteGreat}")
//        println("Excellent: ${review.summary.whiteExcellent}")
//        println("Best: ${review.summary.whiteBest}")
//        println("Good: ${review.summary.whiteGood}")
//        println("Book: ${review.summary.whiteBook}")
//        println("Inaccuracies: ${review.summary.whiteInaccuracies}")
//        println("Mistakes: ${review.summary.whiteMistakes}")
//        println("Misses: ${review.summary.whiteMisses}")
//        println("Blunders: ${review.summary.whiteBlunders}")
//
//        println("\nBlack moves:")
//        println("Brilliant: ${review.summary.blackBrilliant}")
//        println("Great: ${review.summary.blackGreat}")
//        println("Excellent: ${review.summary.blackExcellent}")
//        println("Best: ${review.summary.blackBest}")
//        println("Good: ${review.summary.blackGood}")
//        println("Book: ${review.summary.blackBook}")
//        println("Inaccuracies: ${review.summary.blackInaccuracies}")
//        println("Mistakes: ${review.summary.blackMistakes}")
//        println("Misses: ${review.summary.blackMisses}")
//        println("Blunders: ${review.summary.blackBlunders}")
//
//        reviewer.close()
//    } catch (e: Exception) {
//        println("Error running Stockfish: ${e.message}")
//        e.printStackTrace()
//
//        println("\nTroubleshooting suggestions:")
//        println("1. Make sure stockfish executable is in the current directory")
//        println("2. Check if stockfish has execution permissions")
//        println("3. Try using an absolute path to stockfish")
//        println("4. For Windows, make sure to use 'stockfish.exe' as the filename")
//    }
//}

fun main() {
    val stockfishPath = "E:\\kotlin\\KTOR\\server\\src\\main\\kotlin\\service\\stockfish.exe"

    try {
        val reviewer = ChessGameReviewer(stockfishPath)

        val samplePgn = """
            [Event "Example Game"]
            [Site "Chess.com"]
            [Date "2023.01.15"]
            [Round "1"]
            [White "Player1"]
            [Black "Player2"]
            [Result "1-0"]
            
            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Na5 10. Bc2 c5 
            11. d4 Qc7 12. Nbd2 cxd4 13. cxd4 exd4 14. Nxd4 Nc6 15. Nxc6 Qxc6 16. e5 dxe5 17. Rxe5 Bd6 18. Re1 Bd7 
            19. Nf3 Rae8 20. Rxe8 Rxe8 21. Bg5 Be6 22. Qd4 h6 23. Bxf6 gxf6 24. Qxf6 Bh2+ 25. Kh1 Bf4 26. g3 Be5 
            27. Qf5 Bxg3 28. fxg3 Qxc2 29. Qxe6 Qc4 30. Re1
        """.trimIndent()

        val review = reviewer.reviewGame(samplePgn)

        val resultBuilder = StringBuilder()

        resultBuilder.appendLine("Game: ${review.headers["White"]} vs ${review.headers["Black"]}")
        resultBuilder.appendLine("==================================")

        review.moveEvaluations.forEach { eval ->
            if (eval.playerToMove == "White") {
                resultBuilder.append("${eval.moveNumber}. ")
            }
            resultBuilder.append("${eval.move} ")
            resultBuilder.append("(${eval.evaluation}) [${eval.category}] - ${eval.comment}\n")
        }

        resultBuilder.appendLine("\nGame Summary:")
        resultBuilder.appendLine("==================================")
        resultBuilder.appendLine("Result: ${review.summary.result}")
        resultBuilder.appendLine("\nWhite moves:")
        resultBuilder.appendLine("Brilliant: ${review.summary.whiteBrilliant}")
        resultBuilder.appendLine("Great: ${review.summary.whiteGreat}")
        resultBuilder.appendLine("Excellent: ${review.summary.whiteExcellent}")
        resultBuilder.appendLine("Best: ${review.summary.whiteBest}")
        resultBuilder.appendLine("Good: ${review.summary.whiteGood}")
        resultBuilder.appendLine("Book: ${review.summary.whiteBook}")
        resultBuilder.appendLine("Inaccuracies: ${review.summary.whiteInaccuracies}")
        resultBuilder.appendLine("Mistakes: ${review.summary.whiteMistakes}")
        resultBuilder.appendLine("Misses: ${review.summary.whiteMisses}")
        resultBuilder.appendLine("Blunders: ${review.summary.whiteBlunders}")

        resultBuilder.appendLine("\nBlack moves:")
        resultBuilder.appendLine("Brilliant: ${review.summary.blackBrilliant}")
        resultBuilder.appendLine("Great: ${review.summary.blackGreat}")
        resultBuilder.appendLine("Excellent: ${review.summary.blackExcellent}")
        resultBuilder.appendLine("Best: ${review.summary.blackBest}")
        resultBuilder.appendLine("Good: ${review.summary.blackGood}")
        resultBuilder.appendLine("Book: ${review.summary.blackBook}")
        resultBuilder.appendLine("Inaccuracies: ${review.summary.blackInaccuracies}")
        resultBuilder.appendLine("Mistakes: ${review.summary.blackMistakes}")
        resultBuilder.appendLine("Misses: ${review.summary.blackMisses}")
        resultBuilder.appendLine("Blunders: ${review.summary.blackBlunders}")

        reviewer.close()

        val formattedGameReview = resultBuilder.toString()

        println(formattedGameReview)


    } catch (e: Exception) {
        println("Error running Stockfish: ${e.message}")
        e.printStackTrace()

        println("\nTroubleshooting suggestions:")
        println("1. Make sure stockfish executable is in the current directory")
        println("2. Check if stockfish has execution permissions")
        println("3. Try using an absolute path to stockfish")
        println("4. For Windows, make sure to use 'stockfish.exe' as the filename")
    }
}

fun getReview(pgn: String):GameReviewResponse? {
    val stockfishPath = "E:\\kotlin\\KTOR\\server\\src\\main\\kotlin\\service\\stockfish.exe"

    try {
        val reviewer = ChessGameReviewer(stockfishPath)

        val review = reviewer.reviewGame(pgn.trimIndent())

        return review

       } catch (e: Exception) {
        println("Error running Stockfish: ${e.message}")
        e.printStackTrace()

        println("\nTroubleshooting suggestions:")
        println("1. Make sure stockfish executable is in the current directory")
        println("2. Check if stockfish has execution permissions")
        println("3. Try using an absolute path to stockfish")
        println("4. For Windows, make sure to use 'stockfish.exe' as the filename")
    }
    return null
}