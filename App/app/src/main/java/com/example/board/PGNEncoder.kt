package com.example.app.board

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.unit.IntOffset
import com.example.app.pieces.Bishop
import com.example.app.pieces.King
import com.example.app.pieces.Knight
import com.example.app.pieces.Pawn
import com.example.app.pieces.Piece
import com.example.app.pieces.Queen
import com.example.app.pieces.Rook

fun encodeToPGN(
    piece: Piece,
    isCheck: Boolean,
    isCheckmate: Boolean,
    capture: Boolean,
    shortCastle: Boolean,
    longCastle: Boolean,
    initialPosition: IntOffset,
    finalPosition: IntOffset,
    promotionPiece: Piece? = null,
    sameRow: Boolean,
    sameColumn: Boolean
): String {
    return when {
        shortCastle -> if(isCheckmate)"O-O#" else if(isCheck)"O-O+" else "O-O"
        longCastle -> if(isCheckmate)"O-O-O#" else if(isCheck)"O-O-O+" else "O-O-O"
        else -> {
            val pieceSymbol = when (piece) {
                is Pawn -> ""
                is Knight -> "N"
                is Bishop -> "B"
                is Rook -> "R"
                is Queen -> "Q"
                is King -> "K"
                else -> throw IllegalArgumentException("Unknown piece type")
            }

            val initialRow = initialPosition.y.toString()//1,2,3,4
            val initialColumn = initialPosition.x.toChar().lowercase().toString()//A,B,C,D

            val finalRow = finalPosition.y.toString()
            val finalColumn = finalPosition.x.toChar().lowercase().toString()

            var move = pieceSymbol

            if (piece is Pawn && capture) {
                move += initialColumn
            }

            if (sameRow) {
                move += initialColumn
            } else if (sameColumn) {
                move += initialRow
            }

            if (capture) {
                move += "x"
            }

            move += finalColumn
            move += finalRow

            if (promotionPiece != null) {
                val promotionPieceSymbol = when (promotionPiece) {
                    is Knight -> "N"
                    is Bishop -> "B"
                    is Rook -> "R"
                    is Queen -> "Q"
                    else -> throw IllegalArgumentException("Unknown piece type")
                }
                move += "=${promotionPieceSymbol}"
            }

            if (isCheckmate) {
                move += "#"
            } else if (isCheck) {
                move += "+"
            }

            move
        }
    }
}

fun ExportPgn(pgn: MutableList<String>): String{
    val count = if(pgn.size%2 == 0) {(pgn.size)/2} else ((pgn.size)/2) + 1
    var result = ""

    for(i in 0 until count){
        result+= "${i+1}. ${pgn[2*i]} "
        if(i+1  < count) {
            result += "${pgn[2 * i + 1]} "
        }
    }

    return result
}

fun sharePgn(context: Context, pgn: String) {
    Log.d("PGN","PGN to share:${pgn}")
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, pgn)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}