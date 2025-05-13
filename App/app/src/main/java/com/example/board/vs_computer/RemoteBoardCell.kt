package com.example.app.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.pieces.Piece
import com.example.app.ui.theme.ActiveColor
import com.example.app.ui.theme.DarkColor
import com.example.app.ui.theme.LightColor

@Composable
fun RemoteBoardCell(
    x: Int,
    y: Int,
    piece: Piece?,
    board: RemoteBoard,
    isAvailableMove: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when {
            piece != null && piece == board.selectedPiece ->
                ActiveColor

            (x + y) % 2 == 0 -> LightColor

            else -> DarkColor
        }

    val textColor =
        when {
            piece != null && piece == board.selectedPiece ->
                Color.White

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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        board.selectPiece(piece = it)
                    }
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        if (isAvailableMove) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        board.moveSelectedPiece(x,y)
                    }
                    .drawBehind {
                        drawCircle(
                            color = ActiveColor,
                            radius = size.width / 6f,
                            center = center,
                        )
                    }
            )
        }
    }
}