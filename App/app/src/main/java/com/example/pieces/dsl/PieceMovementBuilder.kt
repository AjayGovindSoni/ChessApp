package com.example.app.pieces.dsl

import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.Piece

fun Piece.getPieceMoves(
    pieces: List<Piece>,
    bolck: PieceMovementBuilder.() -> Unit
): Set<IntOffset> {
    val builder = PieceMovementBuilder(
        piece = this,
        pieces = pieces
    )
    builder.bolck()
    return builder.build()
}

class PieceMovementBuilder(
    private val piece: Piece,
    private val pieces: List<Piece>,
) {
    private val moves = mutableSetOf<IntOffset>()

    fun straightMoves(
        maxMovements: Int = 7,
        canCapture: Boolean = true,
        captureOnly: Boolean = false,
    ) {
        StraightMovement.entries.forEach { movement ->
            straightMoves(
                movement = movement,
                maxMovements = maxMovements,
                canCapture = canCapture,
                captureOnly = captureOnly
            )
        }
    }

    fun diagonalMoves(
        maxMovements: Int = 7,
        canCapture: Boolean = true,
        captureOnly: Boolean = false,
    ) {
        DiagonalMovement.entries.forEach { movement ->
            diagonalMoves(
                movement = movement,
                maxMovements = maxMovements,
                canCapture = canCapture,
                captureOnly = captureOnly
            )
        }
    }


    fun straightMoves(
        movement: StraightMovement,
        maxMovements: Int = 7,
        canCapture: Boolean = true,
        captureOnly: Boolean = false,
    ) {
        moves.addAll(
            piece.getStraightMoves(
                pieces = pieces,
                movement = movement,
                maxMovements = maxMovements,
                canCapture = canCapture,
                captureOnly = captureOnly
            )
        )
    }

    fun diagonalMoves(
        movement: DiagonalMovement,
        maxMovements: Int = 7,
        canCapture: Boolean = true,
        captureOnly: Boolean = false,
    ) {
        moves.addAll(
            piece.getDiagonalMoves(
                pieces = pieces,
                movement = movement,
                maxMovements = maxMovements,
                canCapture = canCapture,
                captureOnly = captureOnly
            )
        )
    }

    fun getLMoves() {
        moves.addAll(
            piece.getLMoves(pieces = pieces)
        )
    }

    fun En_passant(
        movement: DiagonalMovement,
        maxMovements: Int = 1,
        canCapture: Boolean = true,
        captureOnly: Boolean = false,
    ) {
        moves.addAll(
            piece.getEn_passant(
                pieces = pieces,
                movement = movement,
                maxMovements = maxMovements,
                canCapture = canCapture,
                captureOnly = captureOnly
            )
        )
    }

    fun Castle(
        movement: StraightMovement,
        maxMovements: Int = 2,
        canCapture: Boolean = false,
        captureOnly: Boolean = false
    ){
        moves.addAll(
            piece.getCastle(
                pieces = pieces,
                movement = movement,
                maxMovements = maxMovements,
                canCapture = canCapture,
                captureOnly = captureOnly
            )
        )
    }

    fun build(): Set<IntOffset> = moves.toSet()


}