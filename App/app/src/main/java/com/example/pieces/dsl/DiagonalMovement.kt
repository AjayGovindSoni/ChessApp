package com.example.app.pieces.dsl

import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.Piece

enum class DiagonalMovement{
    UpLeft,
    UpRight,
    DownLeft,
    DownRight
}

fun Piece.getDiagonalMoves(
    pieces: List<Piece>,
    maxMovements: Int = 7,
    movement: DiagonalMovement,
    canCapture: Boolean = true,
    captureOnly: Boolean = false,
): Set<IntOffset> {
    return getMoves(pieces = pieces,
        getPosition = {
            when(movement){
                com.example.app.pieces.dsl.DiagonalMovement.UpLeft ->
                    IntOffset(
                        x = position.x - it,
                        y = position.y + it
                    )
                com.example.app.pieces.dsl.DiagonalMovement.UpRight ->
                    IntOffset(
                        x = position.x + it,
                        y = position.y + it
                    )
                com.example.app.pieces.dsl.DiagonalMovement.DownLeft ->
                    IntOffset(
                        x = position.x - it,
                        y = position.y - it
                    )
                com.example.app.pieces.dsl.DiagonalMovement.DownRight ->
                    IntOffset(
                        x = position.x + it,
                        y = position.y - it
                    )
            }
        },
        maxMovements = maxMovements,
        canCapture = canCapture,
        captureOnly = captureOnly
    )
}