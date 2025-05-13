package com.example.app.pieces

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.unit.IntOffset
import com.example.app.R
import com.example.app.pieces.dsl.StraightMovement
import com.example.app.pieces.dsl.getPieceMoves


@VisibleForTesting
class King(
    override val color: Piece.Color,
    override var position: IntOffset
) : Piece {

    override val type: Char = Type

    var is_moved = false

    override val drawable: Int =
        if (color.isWhite) {
            R.drawable.piece_king__side_white
        } else {
            R.drawable.piece_king__side_black
        }

    override fun getAvailableMoves(pieces: List<Piece>): Set<IntOffset> =
        getPieceMoves(pieces) {
            straightMoves(maxMovements = 1)
            diagonalMoves(maxMovements = 1)
            if (!is_moved) {
                val leftRook =
                    pieces.firstOrNull { it.color == color && it.position.y == position.y && it.position.x == 65 && it is Rook }
                val rightRook =
                    pieces.firstOrNull { it.color == color && it.position.y == position.y && it.position.x == 72 && it is Rook }
                if (leftRook != null && leftRook is Rook && !leftRook.is_moved) {
                    Castle(movement = StraightMovement.LEFT)
                }
                if (rightRook != null && rightRook is Rook && !rightRook.is_moved) {
                    Castle(movement = StraightMovement.RIGHT)
                }

            }

        }

    companion object {
        const val Type = 'K'
    }
}