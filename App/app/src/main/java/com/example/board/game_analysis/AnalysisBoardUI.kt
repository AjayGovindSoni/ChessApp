package com.example.board.game_analysis

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.board.BoardXCoordinates
import com.example.app.board.BoardYCoordinates
import com.example.app.board.rememberPieceAt

@Composable
fun AnalysisBoardUI(
    board: AnalysisBoard,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .border(
                width = 8.dp,
                color = Color.White
            )
            .padding(8.dp)
    ) {
        BoardYCoordinates
            .forEach { y ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    BoardXCoordinates
                        .forEach { x ->
                            val piece = board.getPiece(x, y)

                            AnalysisBoardCell(
                                x = x,
                                y = y,
                                piece = piece,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }
                }
            }
    }
}