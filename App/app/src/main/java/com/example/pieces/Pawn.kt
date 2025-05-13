package com.example.app.pieces

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.unit.IntOffset
import com.example.app.R
import com.example.app.pieces.Rook.Companion
import com.example.app.pieces.dsl.DiagonalMovement
import com.example.app.pieces.dsl.StraightMovement
import com.example.app.pieces.dsl.getPieceMoves


@VisibleForTesting
class Pawn(
    override val color: Piece.Color,
    override var position: IntOffset
) : Piece {

    override val type: Char = Type

    override val drawable: Int =
        if (color.isWhite) {
            R.drawable.piece_pawn__side_white
        } else {
            R.drawable.piece_pawn__side_black
        }

    var isFirstMove =
        position.y == 2 && color.isWhite ||
                position.y == 7 && color.isBlack

    var en_enpassent : Boolean = false

    override fun getAvailableMoves(pieces: List<Piece>): Set<IntOffset> {
        isFirstMove =
            position.y == 2 && color.isWhite ||
                    position.y == 7 && color.isBlack
        return getPieceMoves(pieces){
            straightMoves(
                movement = if (color.isWhite) StraightMovement.UP else StraightMovement.DOWN,
                maxMovements = if (isFirstMove) 2 else 1,
                canCapture = false,
            )
            diagonalMoves(

                movement = if (color.isWhite) DiagonalMovement.UpRight else DiagonalMovement.DownRight,
                maxMovements = if (isFirstMove) 2 else 1,
                captureOnly = true,
            )

            diagonalMoves(
                movement = if (color.isWhite) DiagonalMovement.UpLeft else DiagonalMovement.DownLeft,
                maxMovements = if (isFirstMove) 2 else 1,
                captureOnly = true,)

            pieces.firstOrNull { adjacentPiece ->
                adjacentPiece is Pawn &&
                        adjacentPiece.color != this@Pawn.color &&
                        adjacentPiece.en_enpassent &&
                        (adjacentPiece.position == position + IntOffset(1, 0) || adjacentPiece.position == position + IntOffset(-1, 0))
            }?.let { adjacentPawn ->
                // Determine the correct diagonal move based on the adjacent pawn's position
                if (adjacentPawn.position.x > this@Pawn.position.x) {
                    En_passant(
                        movement = if (color.isWhite) DiagonalMovement.UpRight else DiagonalMovement.DownRight,
                        maxMovements = 1,
                    )
                } else {
                    En_passant(
                        movement = if (color.isWhite) DiagonalMovement.UpLeft else DiagonalMovement.DownLeft,
                        maxMovements = 1,
                    )
                }
            }

        }
    }

    companion object{
        const val Type = 'P'
    }
}