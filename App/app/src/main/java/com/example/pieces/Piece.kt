package com.example.app.pieces

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.IntOffset
import com.example.app.board.BoardXCoordinates
import com.example.app.board.BoardYCoordinates

interface Piece {

    val color: Color

    enum class Color {
        White,
        Black;

        val isWhite: Boolean
            get() = this == White
        val isBlack: Boolean
            get() = this == Black
    }

    val type: Char

    @get:DrawableRes
    val drawable: Int

    var position: IntOffset

    fun getAvailableMoves(pieces: List<Piece>): Set<IntOffset>

    fun encode(): String {
        // color code saved as W for white and B for Black
        val colorCode = color.name.first()

        return StringBuilder()
            .append(type)
            .append(colorCode)
            .append(position.x - BoardXCoordinates.min())
            .append(position.y - BoardYCoordinates.min())
            .toString()
    }

    companion object {
        fun decode(encodedPiece: String): Piece {
            val (type, color, x, y) = encodedPiece.toCharArray()

            val pieceColor =
                Color.entries.find { it.name.first() == color }
                    ?: throw IllegalArgumentException("Invalid piece color!")

            val position =
                IntOffset(
                    x = x.digitToInt() + BoardXCoordinates.min(),
                    y = y.digitToInt() + BoardYCoordinates.min()
                )

            return when (type) {
                Pawn.Type ->
                    Pawn(pieceColor, position)

                King.Type ->
                    King(pieceColor, position)

                Queen.Type ->
                    Queen(pieceColor, position)

                Knight.Type ->
                    Knight(pieceColor, position)

                Rook.Type ->
                    Rook(pieceColor, position)

                Bishop.Type ->
                    Bishop(pieceColor, position)

                else ->
                    throw IllegalArgumentException("Invalid piece color!")
            }
        }

        const val EncodedPieceLength = 4
    }
}