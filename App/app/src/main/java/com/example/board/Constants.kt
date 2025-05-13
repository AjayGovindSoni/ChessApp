package com.example.app.board

import com.example.app.pieces.Piece

// A to H

val BoardXCoordinates = List(8){
    'A'.code + it
}

// 9 to 1

val BoardYCoordinates =List(8){
    8 - it
}

const val InitialEncodedPiecesPosition =
    "PW01PB06PW11PB16PW21PB26PW31PB36PW41PB46PW51PB56PW61PB66PW71PB76RW00RW70RB07RB77NW10NW60NB17NB67BW20BW50BB27BB57KW40KB47QW30QB37"


fun decodePieces(
    encodedPieces: String
):List<Piece>{
    val pieces = mutableListOf<Piece>()

    var index = 0
    while (index < encodedPieces.length){
        val encodedPiece = encodedPieces.substring(index, index+Piece.EncodedPieceLength)
        pieces.add(Piece.decode(encodedPiece))
        index+=  Piece.EncodedPieceLength
    }
    return pieces
}

fun encodePieces(
    decodedPieces: List<Piece>
): String {
    var encodedString = ""
    for (piece in decodedPieces) {
        encodedString += piece.encode()
    }
    return encodedString
}