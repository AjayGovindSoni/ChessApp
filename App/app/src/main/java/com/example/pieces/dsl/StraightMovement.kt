package com.example.app.pieces.dsl

import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.Piece

fun Piece.getStraightMoves(
    pieces: List<Piece>,
    maxMovements: Int = 7,
    movement: StraightMovement,
    canCapture: Boolean = true,
    captureOnly: Boolean = false,
): Set<IntOffset> {
    return getMoves(pieces = pieces,
        getPosition = {
            when(movement){
                com.example.app.pieces.dsl.StraightMovement.UP ->
                    IntOffset(
                        x = position.x,
                        y = position.y + it
                    )
                com.example.app.pieces.dsl.StraightMovement.DOWN ->
                    IntOffset(
                        x = position.x,
                        y = position.y - it
                    )
                com.example.app.pieces.dsl.StraightMovement.LEFT ->
                    IntOffset(
                        x = position.x - it,
                        y = position.y
                    )
                com.example.app.pieces.dsl.StraightMovement.RIGHT ->
                    IntOffset(
                        x = position.x + it,
                        y = position.y
                    )
            }
        },
        maxMovements = maxMovements,
        canCapture = canCapture,
        captureOnly = captureOnly
    )
}