package com.example.board.game_analysis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import com.example.app.board.InitialEncodedPiecesPosition
import com.example.app.board.decodePieces
import com.example.app.board.encodePieces
import com.example.app.pieces.King
import com.example.app.pieces.Pawn
import com.example.app.pieces.Piece
import com.example.app.pieces.Rook
import kotlin.math.absoluteValue


@Composable
fun rememberAnalysisBoard(): AnalysisBoard = remember {
    AnalysisBoard()
}

class AnalysisBoard() {
    private val _pieces = mutableStateListOf<Piece>()
    val pieces get() = _pieces.toList()

    init {
        _pieces.addAll(
            decodePieces(InitialEncodedPiecesPosition)
        )
    }

    val positions = mutableStateListOf<String>(InitialEncodedPiecesPosition)

    fun getPiece(x: Int, y: Int): Piece? = _pieces.find { it.position.x == x && it.position.y == y }

    fun moveForward(move: String) {
        var x1 = move[0].uppercaseChar().code
        var y1 = move[1].digitToInt()
        var x2 = move[2].uppercaseChar().code
        var y2 = move[3].digitToInt()
        val initialPosition: IntOffset = IntOffset(x1, y1)
        val finalPosition: IntOffset = IntOffset(x2, y2)
        val piece = getPiece(initialPosition.x, initialPosition.y)
        if (piece != null) {
            movePiece(piece, finalPosition)
            positions.add(encodePieces(pieces))
        }
    }

    private fun removePiece(piece: Piece) {
        _pieces.remove(piece)
    }

    private fun movePiece(
        piece: Piece, position: IntOffset
    ) {
        val targetPiece = pieces.find { it.position == position }

        if (targetPiece != null) {
            removePiece(targetPiece)
        }

        if (piece is Pawn && targetPiece == null) {
            val adjacentPawn = pieces.firstOrNull { adjacentPiece ->
                adjacentPiece is Pawn && adjacentPiece.color != piece.color && adjacentPiece.en_enpassent && (adjacentPiece.position == piece.position + IntOffset(
                    1,
                    0
                ) || adjacentPiece.position == piece.position + IntOffset(-1, 0))
            }
            if (adjacentPawn != null && position.x == adjacentPawn.position.x) {
                removePiece(adjacentPawn)
            }
        }

        if (piece is Pawn && piece.isFirstMove && (piece.position.y - position.y).absoluteValue == 2) {
            piece.en_enpassent = true
        }

        if (piece is King) {
            if (position.x == piece.position.x + 2) {
                val rook =
                    _pieces.firstOrNull { it.color == piece.color && it.position.y == piece.position.y && it.position.x == 72 && it is Rook }
                if (rook != null && rook is Rook) {
                    rook.position = IntOffset(position.x - 1, position.y)
                    rook.is_moved = true
                }
            }
            if (position.x == piece.position.x - 2) {
                val rook =
                    _pieces.firstOrNull { it.color == piece.color && it.position.y == piece.position.y && it.position.x == 65 && it is Rook }
                if (rook != null && rook is Rook) {
                    rook.position = IntOffset(position.x + 1, position.y)
                    rook.is_moved = true
                }
            }
        }
        piece.position = position
    }

    fun loadPosition(encoded: String) {
        _pieces.clear()
        _pieces.addAll(decodePieces(encoded))
    }


}

@Composable
fun AnalysisBoard.rememberPieceAt(x: Int, y: Int): Piece? = remember(x, y) {
    getPiece(
        x = x,
        y = y,
    )
}

