package com.example.app.board

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.*
import kotlin.math.absoluteValue

@Composable
fun rememberRemoteBoard(): RemoteBoard = remember { RemoteBoard() }

@Immutable
class RemoteBoard {
    private val _pieces = mutableStateListOf<Piece>()
    val pieces get() = _pieces.toList()

    var gameOver by mutableStateOf(false)
        private set

    init {
        _pieces.addAll(decodePieces(InitialEncodedPiecesPosition))
    }

    var selectedPiece by mutableStateOf<Piece?>(null)
        private set

    var selectedPieceMoves by mutableStateOf(emptySet<IntOffset>())
        private set

    var moveIncrement by mutableIntStateOf(0)
        private set

    var playerTurn by mutableStateOf(Piece.Color.White)

    fun selectPiece(piece: Piece) {
        if (piece.color != playerTurn) return
        if (piece == selectedPiece) {
            clearSelection()
        } else {
            selectedPiece = piece
            selectedPieceMoves = getLegalMoves(piece, pieces)
        }
    }

    fun moveSelectedPiece(x: Int, y: Int) {
        selectedPiece?.let { piece ->
            if (piece.color != playerTurn) return
            val legalMoves = getLegalMoves(piece, pieces)
            if (IntOffset(x, y) !in legalMoves) return

            movePiece(piece, IntOffset(x, y))
            handlePostMove(piece)
        }
    }

    private fun handlePostMove(piece: Piece) {
        if (piece is Pawn && ((piece.color.isWhite && piece.position.y == 8) || (piece.color.isBlack && piece.position.y == 1))) {
            promotePiece(piece)
        }
        if (piece is King) {
            piece.is_moved = true
        }
        if (piece is Rook) {
            piece.is_moved = true
        }

        clearSelection()
        switchPlayerTurn()

        if (isCheck(pieces, playerTurn)) {
            Log.d("ChessGame", "King is in check!")
            if (isCheckmate(pieces, playerTurn)) {
                Log.d("ChessGame", "Checkmate! Game over.")
                gameOver = true
                return
            }
        }

        pieces.filterIsInstance<Pawn>().forEach { pawn ->
            if (pawn.color == playerTurn) pawn.en_enpassent = false
        }

        moveIncrement++
        if (playerTurn == Piece.Color.Black) remotePlayerMove() // AI Move
    }

    private fun remotePlayerMove() {
        val aiPieces = pieces.filter { it.color == Piece.Color.Black }
        val moveCandidates = aiPieces.flatMap { piece ->
            getLegalMoves(piece, pieces).map { move -> piece to move }
        }

        if (moveCandidates.isNotEmpty()) {
            val (piece, move) = moveCandidates.random()
            movePiece(piece, move)
            handlePostMove(piece)
        }
    }

    fun getPiece(x: Int, y: Int): Piece? = _pieces.find { it.position.x == x && it.position.y == y }

    fun isAvailableMove(x: Int, y: Int): Boolean = selectedPieceMoves.any { it.x == x && it.y == y }

    fun restartGame() {
        _pieces.clear()
        _pieces.addAll(decodePieces(InitialEncodedPiecesPosition))
        gameOver = false
        playerTurn = Piece.Color.White
        selectedPiece = null
        selectedPieceMoves = emptySet()
        moveIncrement = 0
    }

    private fun movePiece(piece: Piece, position: IntOffset) {
        val targetPiece = pieces.find { it.position == position }
        if (targetPiece != null) removePiece(targetPiece)

        if (piece is Pawn && targetPiece == null) {
            val adjacentPawn = pieces.firstOrNull { adjacentPiece ->
                adjacentPiece is Pawn && adjacentPiece.color != piece.color && adjacentPiece.en_enpassent &&
                        (adjacentPiece.position == piece.position + IntOffset(
                            1,
                            0
                        ) || adjacentPiece.position == piece.position + IntOffset(-1, 0))
            }
            if (adjacentPawn != null && position.x == adjacentPawn.position.x) removePiece(
                adjacentPawn
            )
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

    private fun removePiece(piece: Piece) {
        _pieces.remove(piece)
    }

    private fun clearSelection() {
        selectedPiece = null
        selectedPieceMoves = emptySet()
    }

    private fun switchPlayerTurn() {
        playerTurn = if (playerTurn.isWhite) Piece.Color.Black else Piece.Color.White
    }

    private fun getLegalMoves(piece: Piece, pieces: List<Piece>): Set<IntOffset> {
        val availableMoves = piece.getAvailableMoves(pieces)

        val isKingInCheck = isCheck(pieces, piece.color)

        return availableMoves.filter { move ->
            val simulatedPieces = pieces.toMutableList()
            val simulatedPiece = simulatedPieces.find { it == piece }!!
            val originalPosition = simulatedPiece.position

            val targetPiece = simulatedPieces.find { it.position == move }
            simulatedPieces.remove(targetPiece)
            simulatedPiece.position = move

            val kingInCheck = isCheck(simulatedPieces, piece.color)

            simulatedPiece.position = originalPosition
            if (targetPiece != null) simulatedPieces.add(targetPiece)

            if (piece is King && isKingInCheck) {
                val isCastlingMove = (move.x - originalPosition.x).absoluteValue == 2
                if (isCastlingMove) return@filter false
            }

            if (piece is King) {
                val isCastlingMove = (move.x - originalPosition.x).absoluteValue == 2
                if (isCastlingMove) {
                    val rookX = if (move.x > originalPosition.x) move.x - 1
                    else move.x + 1

                    val rookPosition = IntOffset(rookX, move.y)

                    val simulatedRook = Rook(position = rookPosition, color = piece.color)
                    simulatedPieces.add(simulatedRook)
                    Log.d("SimulatedRook", "${simulatedRook}")
                    if (simulatedRook is Rook && rookUnderAttack(
                            simulatedRook,
                            simulatedPieces,
                            piece.color
                        )
                    ) {
                        return@filter false
                    }
                }
            }

            !kingInCheck
        }.toSet()
    }


    private fun promotePiece(piece: Piece) {
        val newPiece = Queen(position = piece.position, color = piece.color)
        removePiece(piece)
        _pieces.add(newPiece)
    }
}

@Composable
fun RemoteBoard.rememberPieceAt(x: Int, y: Int): Piece? =
    remember(x, y, moveIncrement) { getPiece(x, y) }

@Composable
fun RemoteBoard.rememberIsAvailableMove(x: Int, y: Int): Boolean =
    remember(x, y, selectedPieceMoves) { isAvailableMove(x, y) }

@Composable
fun RemoteBoardGameOverDialog(remoteBoard: RemoteBoard) {
    if (remoteBoard.gameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text("Checkmate! The game is over.") },
            confirmButton = {
                Button(onClick = { remoteBoard.restartGame() }) {
                    Text("New Game")
                }
            })
    }
}
