package com.example.app.pieces.dsl

import androidx.compose.ui.unit.IntOffset
import com.example.app.board.BoardXCoordinates
import com.example.app.board.BoardYCoordinates
import com.example.app.pieces.Piece

enum class StraightMovement {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

fun Piece.getMoves(
    pieces: List<Piece>,
    getPosition: (Int) -> IntOffset,
    maxMovements: Int,
    canCapture: Boolean,
    captureOnly: Boolean,
): Set<IntOffset> {
    val moves = mutableSetOf<IntOffset>()

    for (i in 1..maxMovements) {
        val targetPosition = getPosition(i)

        if (targetPosition.x !in BoardXCoordinates || targetPosition.y !in BoardYCoordinates)
            break

        val targetPiece = pieces.find { it.position == targetPosition }

        if (targetPiece != null) {
            if (targetPiece.color != this.color && canCapture)
                moves.add(targetPosition)
            break
        } else if (captureOnly) {
            break
        } else {
            moves.add(targetPosition)
        }
    }
    return moves
}

fun Piece.getLMoves(pieces: List<Piece>): Set<IntOffset> {
    val moves = mutableSetOf<IntOffset>()

    val offsets = listOf(
        IntOffset(-1, -2),
        IntOffset(1, -2),
        IntOffset(-2, -1),
        IntOffset(2, -1),
        IntOffset(-2, 1),
        IntOffset(2, 1),
        IntOffset(-1, 2),
        IntOffset(1, 2)
    )

    for (offset in offsets) {
        val targetPosition = position + offset

        if (targetPosition.x !in BoardXCoordinates || targetPosition.y !in BoardYCoordinates)
            continue

        val targetPiece = pieces.find { it.position == targetPosition }
        if (targetPiece == null || targetPiece.color != this.color) {
            moves.add(targetPosition)
        }
    }

    return moves
}

fun Piece.getEn_passant(
    pieces: List<Piece>,
    maxMovements: Int = 1,
    movement: DiagonalMovement,
    canCapture: Boolean = true,
    captureOnly: Boolean = false,
): Set<IntOffset> {
    return getMoves(
        pieces = pieces,
        getPosition = {
            when (movement) {
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

fun Piece.getCastle(
    pieces: List<Piece>,
    maxMovements: Int = 2,
    movement: StraightMovement,
    canCapture: Boolean = false,
    captureOnly: Boolean = false,
): Set<IntOffset> {
    return getMoves(
        pieces = pieces,
        getPosition = {
            when (movement) {
                StraightMovement.LEFT ->
                    IntOffset(
                        x = position.x - it,
                        y = position.y
                    )

                StraightMovement.RIGHT ->
                    IntOffset(
                        x = position.x + it,
                        y = position.y
                    )

                else -> IntOffset(
                    x = position.x,
                    y = position.y
                )
            }
        },
        maxMovements = maxMovements,
        canCapture = canCapture,
        captureOnly = captureOnly
    )
}
