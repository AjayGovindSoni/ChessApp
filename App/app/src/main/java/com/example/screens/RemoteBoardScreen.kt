package com.example.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.app.board.RemoteBoardGameOverDialog
import com.example.app.board.RemoteBoardUI
import com.example.app.board.rememberRemoteBoard

@Composable
fun RemoteBoardScreen() {
    val board = rememberRemoteBoard()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        RemoteBoardUI(board = board)
        RemoteBoardGameOverDialog(board)
    }
}

@Preview
@Composable
fun PreviewRemoteBoardScreen() {
    RemoteBoardScreen()
}
