package com.example.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app.pieces.Piece
import com.example.board.online_game.ClockDisplay
import com.example.board.online_game.GameOverDialog
import com.example.board.online_game.OnlineBoardUI
import com.example.board.online_game.rememberOnlineBoard


@Composable
fun OnlineBoardScreen(
    id: String, navController: NavController
) {
    val board = rememberOnlineBoard(
        id = id
    )
    if (board.localPlayerColor == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (board.localPlayerColor == Piece.Color.White) {
            ClockDisplay(board.blackTimeLeft.value, Piece.Color.Black)
            OnlineBoardUI(board = board)
            ClockDisplay(board.whiteTimeLeft.value, Piece.Color.White)
        } else {
            ClockDisplay(board.whiteTimeLeft.value, Piece.Color.White)
            OnlineBoardUI(board = board)
            ClockDisplay(board.blackTimeLeft.value, Piece.Color.Black)
        }
        GameOverDialog(board = board, pgn = board.exportPGN, navController = navController)
        Log.d("PGN", "Final PGN: ${board.exportPGN}")

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            board.message = "${board.localPlayerColor} Resigns"
            board.resign()
        }) {
            Text("Resign")
        }
    }
}
//
//@Preview
//@Composable
//fun PreviewOnlineBoardScreen() {
//    OnlineBoardScreen(id = "5883e3ea-bf73-44af-a606-740482c1dfaf")
//}
