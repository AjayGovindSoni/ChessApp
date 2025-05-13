package com.example.app.pieces

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.unit.IntOffset
import com.example.app.R
import com.example.app.pieces.Rook.Companion
import com.example.app.pieces.dsl.StraightMovement
import com.example.app.pieces.dsl.getPieceMoves


@VisibleForTesting
class Bishop(
    override val color: Piece.Color,
    override var position: IntOffset
) : Piece {

    override val type: Char = Type

    override val drawable: Int =
        if (color.isWhite) {
            R.drawable.piece_bishop__side_white
        } else {
            R.drawable.piece_bishop__side_black
        }

    override fun getAvailableMoves(pieces: List<Piece>): Set<IntOffset> =
        getPieceMoves(pieces){
            diagonalMoves()
        }

    companion object{
        const val Type = 'B'
    }
}