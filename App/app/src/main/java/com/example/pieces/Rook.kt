package com.example.app.pieces

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.unit.IntOffset
import com.example.app.R
import com.example.app.pieces.dsl.StraightMovement
import com.example.app.pieces.dsl.getPieceMoves


@VisibleForTesting
class Rook(
    override val color: Piece.Color,
    override var position: IntOffset
) : Piece {

    override val type: Char = Type

    var is_moved = false

    override val drawable: Int =
        if (color.isWhite) {
            R.drawable.piece_rook__side_white
        } else {
            R.drawable.piece_rook__side_black
        }

    override fun getAvailableMoves(pieces: List<Piece>): Set<IntOffset> =
        getPieceMoves(pieces){
            straightMoves()
        }

    companion object{
        const val Type = 'R'
    }
}