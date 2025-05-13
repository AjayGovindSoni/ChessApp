package com.example.app.board

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.Bishop
import com.example.app.pieces.King
import com.example.app.pieces.Knight
import com.example.app.pieces.Pawn
import com.example.app.pieces.Piece
import com.example.app.pieces.Queen
import com.example.app.pieces.Rook
import kotlin.math.absoluteValue

@Composable
fun rememberBoard(): Board = remember {
    Board()
}

@Immutable
class Board {
    private val _pieces = mutableStateListOf<Piece>()
    val pieces get() = _pieces.toList()

    var gameOver by mutableStateOf(false)
        private set

    var exportPGN by mutableStateOf("")
        private set

    init {
        _pieces.addAll(
            decodePieces(InitialEncodedPiecesPosition)
        )
    }

    var selectedPiece by mutableStateOf<Piece?>(null)
        private set

    var selectedPieceMoves by mutableStateOf(emptySet<IntOffset>())
        private set

    var moveIncrement by mutableIntStateOf(0)
        private set

    var playerTurn by mutableStateOf(Piece.Color.White)


    // variables for pgn encoding
    var isCheck: Boolean = false
    var isCheckmate: Boolean = false
    var capture: Boolean = false
    var shortCastle: Boolean = false
    var longCastle: Boolean = false
    var initialPosition: IntOffset = IntOffset(0, 0)
    var promotionPiece: Piece? = null
    var sameRow: Boolean = false
    var sameColumn: Boolean = false

    var pgn = mutableListOf<String>()


    /*
    * User Events
     */

    fun selectPiece(piece: Piece) {
        if (piece.color != playerTurn) return
        if (piece == selectedPiece) {
            clearSelection()
        } else {
            selectedPiece = piece
            selectedPieceMoves = getLegalMoves(piece = piece, pieces = pieces)
        }
    }

    fun moveSelectedPiece(x: Int, y: Int) {
        selectedPiece?.let { piece ->
            if (piece.color != playerTurn) return
            val legalMoves = getLegalMoves(piece = piece, pieces = pieces) // Get safe moves
            if (IntOffset(x, y) !in legalMoves) return

            initialPosition = piece.position
            movePiece(
                piece = piece, position = IntOffset(x, y)
            )

            if (piece is Pawn) {
                if ((piece.color.isWhite && piece.position.y == 8) || (piece.color.isBlack && piece.position.y == 1)) {
                    promotePiece(piece)
                }
            }

            if (piece is King) {
                piece.is_moved = true
            }
            if (piece is Rook) {
                piece.is_moved = true
            }

            isCheck = isCheck(
                pieces = pieces,
                playerTurn = if (piece.color == Piece.Color.Black) Piece.Color.White else Piece.Color.Black
            )

            isCheckmate = if (isCheck) {
                isCheckmate(
                    pieces = pieces,
                    playerTurn = if (piece.color == Piece.Color.Black) Piece.Color.White else Piece.Color.Black
                )
            } else false

            sameRow = when (piece) {
                is King -> {
                    false
                }

                is Queen -> {
                    false
                }

                is Knight -> {
                    val otherKnight =
                        pieces.firstOrNull { it is Knight && it.position.y == piece.position.y && it.position.x != piece.position.x }
                    if (otherKnight == null) false
                    true
                }

                is Rook -> {
                    val otherRook =
                        pieces.firstOrNull { it is Rook && it.position.y == piece.position.y && it.position.x != piece.position.x }
                    if (otherRook == null) false
                    true
                }

                is Bishop -> {
                    val otherBishop =
                        pieces.firstOrNull { it is Bishop && it.position.y == piece.position.y && it.position.x != piece.position.x }
                    if (otherBishop == null) false
                    true
                }

                else -> {
                    false
                }
            }

            sameColumn = when (piece) {
                is King -> {
                    false
                }

                is Queen -> {
                    false
                }

                is Knight -> {
                    val otherKnight =
                        pieces.firstOrNull { it is Knight && it.position.x == piece.position.x && it.position.y != piece.position.y }
                    if (otherKnight == null) false
                    true
                }

                is Rook -> {
                    val otherRook =
                        pieces.firstOrNull { it is Rook && it.position.x == piece.position.x && it.position.y != piece.position.y }
                    if (otherRook == null) false
                    true
                }

                is Bishop -> {
                    val otherBishop =
                        pieces.firstOrNull { it is Bishop && it.position.x == piece.position.x && it.position.y != piece.position.y}
                    if (otherBishop == null) false
                    true
                }

                else -> {
                    false
                }
            }

            if(pgn.isNotEmpty() && pgn.last().endsWith("#"))
                pgn.clear()
            pgn.add(
                encodeToPGN(
                    piece = piece,
                    isCheck = isCheck,
                    initialPosition = initialPosition,
                    finalPosition = piece.position,
                    capture = capture,
                    shortCastle = shortCastle,
                    longCastle = longCastle,
                    sameRow = sameRow,
                    sameColumn = sameColumn,
                    isCheckmate = isCheckmate,
                    promotionPiece = promotionPiece
                )
            )
            capture = false
            shortCastle = false
            longCastle = false
            promotionPiece = null

            Log.d("PGN","PGN: ${pgn}")

            clearSelection()

            switchPlayerTurn()


            if (isCheck(pieces = pieces, playerTurn = playerTurn)) {
                Log.d("ChessGame", "King is in check!")

                if (isCheckmate(pieces = pieces, playerTurn = playerTurn)) {
                    Log.d("ChessGame", "Checkmate! Game over.")
                    exportPGN = ExportPgn(pgn)
                    Log.d("PGN","Exported PGN: ${exportPGN}")
                    gameOver = true
                }
            }

            pieces.filterIsInstance<Pawn>().forEach { pawn ->
                if (pawn.color == playerTurn) {
                    pawn.en_enpassent = false
                }
            }

            moveIncrement++
        }
    }

    /**
     * Public Methods
     */

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

    /*
    * Private Methods
     */

    private fun movePiece(
        piece: Piece, position: IntOffset
    ) {
        val targetPiece = pieces.find { it.position == position }

        if (targetPiece != null) {
            removePiece(targetPiece)
            capture = true
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
                capture = true
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
                shortCastle = true
            }
            if (position.x == piece.position.x - 2) {
                val rook =
                    _pieces.firstOrNull { it.color == piece.color && it.position.y == piece.position.y && it.position.x == 65 && it is Rook }
                if (rook != null && rook is Rook) {
                    rook.position = IntOffset(position.x + 1, position.y)
                    rook.is_moved = true
                }
                longCastle = true
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
        playerTurn = if (playerTurn.isWhite) {
            Piece.Color.Black
        } else {
            Piece.Color.White
        }
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
        promotionPiece = newPiece
        removePiece(piece)
        _pieces.add(newPiece)
    }
}

@Composable
fun Board.rememberPieceAt(x: Int, y: Int): Piece? = remember(x, y, moveIncrement) {
    getPiece(
        x = x,
        y = y,
    )
}

@Composable
fun Board.rememberIsAvailableMove(x: Int, y: Int): Boolean = remember(x, y, selectedPieceMoves) {
    isAvailableMove(
        x = x,
        y = y,
    )
}

@Composable
fun GameOverDialog(board: Board, pgn: String) {
    val context = LocalContext.current

    if (board.gameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text("Checkmate! The game is over.") },

            confirmButton = {
                Button(onClick = {sharePgn(context, pgn)}) { Text("Export PGN") }
                Button(onClick = { board.restartGame() }) {
                    Text("New Game")
                }
            })
    }
}