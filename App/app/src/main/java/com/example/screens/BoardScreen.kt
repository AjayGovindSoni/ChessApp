package com.example.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.app.board.BoardUI
import com.example.app.board.GameOverDialog
import com.example.app.board.rememberBoard

@Composable
fun BoardScreen() {
    val board = rememberBoard()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        BoardUI(board = board)
        GameOverDialog(board, pgn = board.exportPGN)
        Log.d("PGN", "Final PGN: ${board.exportPGN}")
    }
}

@Preview
@Composable
fun PreviewBoardScreen() {
    BoardScreen()
}
