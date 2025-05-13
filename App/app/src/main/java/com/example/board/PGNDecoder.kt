package com.example.board//package com.example.app.board
//
//import androidx.compose.ui.unit.IntOffset
//import com.example.app.pieces.Bishop
//import com.example.app.pieces.King
//import com.example.app.pieces.Knight
//import com.example.app.pieces.Pawn
//import com.example.app.pieces.Piece
//import com.example.app.pieces.Queen
//import com.example.app.pieces.Rook
//
//
//fun PGNDecoder(pieces: List<Piece>, pgnMove: String) {
//    val piece = when (pgnMove[0]) {
//        'N' -> Knight
//        'B' -> Bishop
//        'R' -> Rook
//        'Q' -> Queen
//        'K' -> King
//        else -> Pawn
//    }
//
//
//
//    val finalRow = pgnMove[pgnMove.length - 2].takeIf { it in 'a'..'h' } ?: ' '
//    val finalColumn = pgnMove.last().takeIf { it in '1'..'8' } ?: ' '
//
//    println("final row: $finalRow")
//
//    println("final column: $finalColumn")
//}