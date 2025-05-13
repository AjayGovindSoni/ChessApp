package com.example.app.board

import android.util.Log
import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.Bishop
import com.example.app.pieces.King
import com.example.app.pieces.Knight
import com.example.app.pieces.Pawn
import com.example.app.pieces.Piece
import com.example.app.pieces.Queen
import com.example.app.pieces.Rook

fun isTheKingInThreat(
    pieces: List<Piece>,
    piece: Piece,
    x: Int,
    y: Int
): Boolean {
    val piecePosition = piece.position
    piece.position = IntOffset(x = x, y = y)
    val king = pieces.firstOrNull { it is King && it.color == piece.color } ?: return false
    val newPieces = pieces.filter { it.position != piece.position } + king
    val enemyPieces = newPieces.filter { it.color != piece.color }
    val enemyMoves = enemyPieces.flatMap { it.getAvailableMoves(pieces = newPieces) }
    val isTheKingInThreat = enemyMoves.any { it == king.position }

    piece.position = piecePosition
    return isTheKingInThreat
}

fun isCheckmate(
    pieces: List<Piece>,
    playerTurn: Piece.Color,
): Boolean {
    val king = pieces.firstOrNull { it is King && it.color == playerTurn } ?: return false
    val kingMoves = king.getAvailableMoves(pieces = pieces) + king.position

    if (!isCheck(pieces, playerTurn)) {
        return false
    }

    val kingEscape = kingMoves.all { kingMove ->
        isTheKingInThreat(
            pieces = pieces,
            piece = king,
            x = kingMove.x,
            y = kingMove.y
        )
    }
    Log.d("CHECKMATE","King Escape:${kingEscape}")

    val friendlyPieces = pieces.filter { it.color == playerTurn && it != king }
    val canCaptureOrBlock = friendlyPieces.any { friendlyPiece ->
        val friendlyPieceMoves = friendlyPiece.getAvailableMoves(pieces)
        friendlyPieceMoves.any { move ->
            val tempPieces = pieces.filter { it.position != friendlyPiece.position }.toMutableList()
            val tempFriendlyPiece = when (friendlyPiece) {
                is Pawn -> Pawn(friendlyPiece.color, move)
                is Rook -> Rook(friendlyPiece.color, move)
                is Knight -> Knight(friendlyPiece.color, move)
                is Bishop -> Bishop(friendlyPiece.color, move)
                is Queen -> Queen(friendlyPiece.color, move)
                is King -> King(friendlyPiece.color, move)
                else -> throw IllegalArgumentException("Unknown piece type")
            }
            tempPieces.add(tempFriendlyPiece)
            val tempKing = tempPieces.find { it is King && it.color == playerTurn }!!

            if (isCheck(tempPieces, playerTurn)) {
                return@any false // Move is illegal because it exposes the king to check
            }

            val newEnemyPieces = tempPieces.filter { it.color != playerTurn }
            val newEnemyMoves = newEnemyPieces.flatMap { it.getAvailableMoves(tempPieces) }

            // Check if the move captures the attacking piece or blocks the check
            val attackingPieces = newEnemyPieces.filter { newEnemyMove -> newEnemyMoves.any { it == tempKing.position } }

            attackingPieces.any { attackingPiece ->
                val isProtected = newEnemyPieces.any { enemyPiece ->
                    enemyPiece != attackingPiece && enemyPiece.getAvailableMoves(tempPieces).contains(attackingPiece.position)
                }
                Log.d("CHECKMATE","is Protected:${isProtected}")

                // If the attacking piece is protected, the king should not be able to capture it
                (tempFriendlyPiece !is King || !isProtected) &&
                tempFriendlyPiece.position == attackingPiece.position || !newEnemyMoves.any { it == tempKing.position }
            }
        }
    }
    Log.d("CHECKMATE","Can capture or block:${canCaptureOrBlock}")

    return kingEscape && !canCaptureOrBlock
}
fun isCheck(
    pieces: List<Piece>,
    playerTurn: Piece.Color
): Boolean {
    val king = pieces.firstOrNull { it is King && it.color == playerTurn } ?: return false
    val enemyPieces = pieces.filter { it.color != playerTurn }
    val enemyMoves = enemyPieces.flatMap { it.getAvailableMoves(pieces = pieces) }

    return enemyMoves.count { it == king.position } > 0
}

fun isStalemate(){
    TODO()
}

fun rookUnderAttack(
    rook: Rook,
    pieces: List<Piece>,
    playerTurn: Piece.Color
): Boolean {
    val enemyPieces = pieces.filter { it.color != playerTurn }
    val enemyMoves = enemyPieces.flatMap { it.getAvailableMoves(pieces = pieces) }

    return enemyMoves.count { it == rook.position } > 0
}