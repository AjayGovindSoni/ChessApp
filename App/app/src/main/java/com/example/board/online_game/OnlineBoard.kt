package com.example.board.online_game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.api.WebSocketManager
import com.example.app.board.ExportPgn
import com.example.app.board.InitialEncodedPiecesPosition
import com.example.app.board.decodePieces
import com.example.app.board.encodeToPGN
import com.example.app.board.isCheck
import com.example.app.board.rookUnderAttack
import com.example.app.board.sharePgn
import com.example.app.pieces.King
import com.example.app.pieces.Pawn
import com.example.app.pieces.Piece
import com.example.app.pieces.Piece.Color
import com.example.app.pieces.Queen
import com.example.app.pieces.Rook
import com.example.app.ui.theme.DarkColor
import com.example.app.ui.theme.GreyColor
import com.example.app.ui.theme.LightColor
import com.example.model.MoveData
import com.example.model.OffsetData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.absoluteValue

@Composable
fun rememberOnlineBoard(id: String): OnlineBoard = remember {
    OnlineBoard(id)
}

@Immutable
class OnlineBoard(id: String) {
    var localPlayerColor by mutableStateOf<Piece.Color?>(null)
        private set

    val ws = WebSocketManager(id)
    private val _pieces = mutableStateListOf<Piece>()
    val pieces get() = _pieces.toList()

    var gameOver by mutableStateOf(false)
        private set

    var exportPGN by mutableStateOf("")
        private set

    val whiteTimeLeft = mutableStateOf(10 * 60 * 1000L)
    val blackTimeLeft = mutableStateOf(10 * 60 * 1000L)

    var currentTurn by mutableStateOf(Piece.Color.White) // update this as you already do
    var isRunning = mutableStateOf(false)

    var message by mutableStateOf("")
        set

    private var timerJob: Job? = null

    init {
        _pieces.addAll(decodePieces(InitialEncodedPiecesPosition))
        ws.connect(
            onMessageReceived = { message ->
                Log.d("WebSocket", "Raw message: $message")
                try {
                    val json = JSONObject(message)
                    when (json.getString("type")) {
                        "assign_color" -> {
                            val colorStr = json.getString("color")
                            localPlayerColor = Piece.Color.valueOf(colorStr)
                        }
                        else -> {
                            val raw = json.getString("message")
                            if (raw.contains("resign", ignoreCase = true) || raw.contains("won by Timeout", ignoreCase = true)) {
                                this.message = raw.substring(21)
                                gameOver = true
                                ws.disconnect()
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("WebSocket", "Non-JSON message: $message")
                    if (message.contains(
                            "resign",
                            ignoreCase = true
                        ) || message.contains("won by Timeout", ignoreCase = true)
                    ) {
                        this.message = message.substring(21)
                        gameOver = true
                        ws.disconnect()
                    }
                }
            },
            onMoveReceived = { move ->
                Log.d("WebSocket", "Move received: $move")
                applyMove(move)
            }
        )
    }

    var selectedPiece by mutableStateOf<Piece?>(null)
        private set

    var selectedPieceMoves by mutableStateOf(emptySet<IntOffset>())
        private set

    var moveIncrement by mutableIntStateOf(0)
        private set

    var playerTurn by mutableStateOf(Color.White)

    // PGN metadata
    var isCheck = false
    var isCheckmate = false
    var capture = false
    var shortCastle = false
    var longCastle = false
    var initialPosition = IntOffset(0, 0)
    var promotionPiece: Piece? = null
    var sameRow = false
    var sameColumn = false

    var pgn = mutableListOf<String>()

    /*
     * User Events
     */

    fun startClock() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000L)
                if (playerTurn == Color.White) {
                    whiteTimeLeft.value -= 1000L
                } else {
                    blackTimeLeft.value -= 1000L
                }

                if (whiteTimeLeft.value <= 0 || blackTimeLeft.value <= 0) {
                    withContext(Dispatchers.Main) {
                        message = "${playerTurn.opposite()} won by Timeout"
                        resign()
                    }
                    break
                }
            }
        }
    }

    fun stopClock() {
        timerJob?.cancel()
    }

    fun selectPiece(piece: Piece) {
        if (localPlayerColor == null || piece.color != playerTurn || piece.color != localPlayerColor) return
        selectedPiece = if (piece == selectedPiece) null else piece
        selectedPieceMoves = getLegalMoves(piece, pieces)
    }

    fun moveSelectedPiece(x: Int, y: Int) {
        selectedPiece?.let { piece ->
            if (piece.color != localPlayerColor || piece.color != playerTurn) return
            val destination = IntOffset(x, y)
            if (destination !in getLegalMoves(piece, pieces)) return

            val moveData = MoveData(
                OffsetData(piece.position.x, piece.position.y),
                OffsetData(destination.x, destination.y)
            )
            applyMove(moveData)
            ws.sendMove(moveData)
        }
    }

    fun applyMove(moveData: MoveData) {
        val piece = getPiece(moveData.initial.x, moveData.initial.y) ?: return
        if (piece.color != playerTurn) return

        initialPosition = piece.position
        movePiece(piece, moveData.final)

        if (piece is Pawn && (piece.position.y == 8 || piece.position.y == 1)) {
            promotePiece(piece)
        }

        updatePGNFlags(piece)
        updatePGN(piece)

        clearSelection()
        switchPlayerTurn()

        if (isCheck(pieces, playerTurn)) {
            if (isMate(pieces, playerTurn)) {
                exportPGN = ExportPgn(pgn)
                message = "${playerTurn.opposite()} won by Checkmate "
                ws.disconnect()
                gameOver = true
            }
        }

        moveIncrement++
    }

    private fun updatePGNFlags(piece: Piece) {
        isCheck = isCheck(pieces, playerTurn.opposite())
        isCheckmate = isCheck && isMate(pieces, playerTurn.opposite())
        capture = false
        shortCastle = false
        longCastle = false
        promotionPiece = null
        sameRow = false
        sameColumn = false
    }

    private fun updatePGN(piece: Piece) {
        if (pgn.isNotEmpty() && pgn.last().endsWith("#")) pgn.clear()
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
    }

//    fun restartGame() {
//        _pieces.clear()
//        _pieces.addAll(decodePieces(InitialEncodedPiecesPosition))
//        gameOver = false
//        playerTurn = Piece.Color.White
//        selectedPiece = null
//        selectedPieceMoves = emptySet()
//        moveIncrement = 0
//        pgn.clear()
//    }

    fun getPiece(x: Int, y: Int): Piece? = _pieces.find { it.position.x == x && it.position.y == y }

    fun isAvailableMove(x: Int, y: Int): Boolean = selectedPieceMoves.any { it.x == x && it.y == y }

    fun resign() {
        localPlayerColor?.let { color ->
            ws.sendMessage("${color.name} resign reason: $message")
            gameOver = true
            ws.disconnect()
        }
    }

    private fun movePiece(piece: Piece, position: OffsetData) {
        val targetPiece = pieces.find { it.position == IntOffset(position.x, position.y) }

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
            val castlingDistance = (position.x - piece.position.x).absoluteValue
            if (castlingDistance == 2) {
                val rookX = if (position.x > piece.position.x) 72 else 65
                val rook = getPiece(rookX, position.y) as? Rook
                if (rook != null) {
                    rook.position = if (position.x > piece.position.x)
                        IntOffset(position.x - 1, position.y)
                    else
                        IntOffset(position.x + 1, position.y)
                    rook.is_moved = true
                }
                if (position.x > piece.position.x) shortCastle = true else longCastle = true
            }
        }

        piece.position = IntOffset(position.x, position.y)
        if (piece is Rook) piece.is_moved = true
        if (piece is King) piece.is_moved = true
    }

    private fun promotePiece(piece: Piece) {
        val newPiece = Queen(position = piece.position, color = piece.color)
        promotionPiece = newPiece
        removePiece(piece)
        _pieces.add(newPiece)
    }

    private fun removePiece(piece: Piece) {
        _pieces.remove(piece)
    }

    private fun clearSelection() {
        selectedPiece = null
        selectedPieceMoves = emptySet()
    }

    private fun switchPlayerTurn() {
        playerTurn = playerTurn.opposite()
        pieces.filterIsInstance<Pawn>().forEach { it.en_enpassent = false }
        Log.d("Clock", "$playerTurn")
        startClock()
        Log.d("Clock", "$playerTurn")
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

    private fun Color.opposite() =
        if (this == Color.White) Color.Black else Color.White


    private fun isMate(pieces: List<Piece>, playerTurn: Piece.Color): Boolean {
        return pieces
            .filter { it.color == playerTurn }
            .all { getLegalMoves(it, pieces).isEmpty() }
    }
}


@Composable
fun OnlineBoard.rememberPieceAt(x: Int, y: Int): Piece? = remember(x, y, moveIncrement) {
    getPiece(
        x = x,
        y = y,
    )
}

@Composable
fun OnlineBoard.rememberIsAvailableMove(x: Int, y: Int): Boolean =
    remember(x, y, selectedPieceMoves) {
        isAvailableMove(
            x = x,
            y = y,
        )
    }

@Composable
fun GameOverDialog(board: OnlineBoard, pgn: String,navController: NavController) {
    val context = LocalContext.current

    if (board.gameOver) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Game Over") },
            text = { Text(board.message) },
            confirmButton = {
                Button(onClick = { sharePgn(context, pgn) }) { Text("Export PGN") }
                Button(onClick = { navController.popBackStack() }) {
                    Text("Main Menu")
                }
            }
        )
    }
}

@Composable
fun ClockDisplay(timeMillis: Long, playerColor: Piece.Color) {
    val minutes = (timeMillis / 1000) / 60
    val seconds = (timeMillis / 1000) % 60
    val color = if (playerColor == Piece.Color.White) LightColor else DarkColor

    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        modifier = Modifier
            .padding(8.dp)
            .background(GreyColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        style = LocalTextStyle.current.copy(
            color = color,
            fontSize = 20.sp
        )
    )
}