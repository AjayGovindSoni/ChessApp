package com.example.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.board.InitialEncodedPiecesPosition
import com.example.app.board.encodePieces
import com.example.board.game_analysis.AnalysisBoard
import com.example.board.game_analysis.AnalysisBoardUI
import com.example.board.game_analysis.rememberAnalysisBoard
import com.example.model.GameReviewResponse

@Composable
fun GameAnalysisScreen(review: GameReviewResponse?, navController: NavController) {
    if (review == null) return

    val headers = review.headers
    val moveEvaluation = review.moveEvaluations
    val summary = review.summary

    var i by remember { mutableStateOf(-1) }
    val currentMove = moveEvaluation.getOrNull(i)


    val positions = remember {
        generateAllPositions(
            InitialEncodedPiecesPosition,
            moveEvaluation.map { it.move })
    }
    var currentIndex by remember { mutableStateOf(0) }

    val board = rememberAnalysisBoard()
    LaunchedEffect(currentIndex) {
        board.loadPosition(positions[currentIndex])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text("Position Evaluation: ${currentMove?.evaluation ?: "N/A"}")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Best Move: ${currentMove?.bestMove ?: "N/A"}")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Move Category: ${currentMove?.category ?: "N/A"}")

        Text("Comment: ${currentMove?.comment ?: "N/A"}")

        Spacer(modifier = Modifier.height(16.dp))

        AnalysisBoardUI(board = board)

        Spacer(modifier = Modifier.height(16.dp))

//        Button(
//            onClick = {
//                currentMove?.move?.let { board.moveForward(it) }
//                if (i < moveEvaluation.lastIndex) i++
//                Log.d("currentMove","$currentMove")
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Next Move")
//        }
        Row {
            Button(onClick = {
                if (currentIndex < positions.lastIndex) {
                    currentIndex++
                    i++
                }
            }) {
                Text("Next Move")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                if (currentIndex >= 0) {
                    currentIndex--
                    i--
                }
            }) {
                Text("Prev Move")
            }
        }
    }
}

fun generateAllPositions(initial: String, moves: List<String>): List<String> {
    val board = AnalysisBoard()
    val positions = mutableListOf(initial)
    for (move in moves) {
        board.moveForward(move)
        positions.add(encodePieces(board.pieces))
    }
    return positions
}