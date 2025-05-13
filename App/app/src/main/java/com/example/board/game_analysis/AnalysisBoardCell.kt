package com.example.board.game_analysis


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.board.BoardXCoordinates
import com.example.app.pieces.Piece
import com.example.app.ui.theme.DarkColor
import com.example.app.ui.theme.LightColor

@Composable
fun AnalysisBoardCell(
    x: Int,
    y: Int,
    piece: Piece?,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when {
            (x + y) % 2 == 0 -> LightColor

            else -> DarkColor
        }

    val textColor =
        when {
            (x + y) % 2 == 0 -> DarkColor

            else -> LightColor
        }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (x == BoardXCoordinates.first()) {
            // Draw Y test
            Text(
                text = y.toString(),
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
            )
        }
        if (y == 1) {
            // Draw X test
            Text(
                text = x.toChar().toString(),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
            )
        }

        piece?.let {
            Image(
                painter = painterResource(it.drawable),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

    }
}