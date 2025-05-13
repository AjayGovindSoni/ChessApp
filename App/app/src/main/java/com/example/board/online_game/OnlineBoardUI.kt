package com.example.board.online_game


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
import com.example.app.pieces.Piece

@Composable
fun OnlineBoardUI(
    board: OnlineBoard,
    modifier: Modifier = Modifier,
) {
    val isWhite = board.localPlayerColor == Piece.Color.White

    val yCoords = if (isWhite) BoardYCoordinates else BoardYCoordinates.reversed()
    val xCoords = if (isWhite) BoardXCoordinates else BoardXCoordinates.reversed()

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
        yCoords.forEach { y ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                xCoords.forEach { x ->
                    val piece = board.rememberPieceAt(x, y)
                    val isAvailableMove = board.rememberIsAvailableMove(x, y)

                    OnlineBoardCell(
                        x = x,
                        y = y,
                        piece = piece,
                        board = board,
                        isAvailableMove = isAvailableMove,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
