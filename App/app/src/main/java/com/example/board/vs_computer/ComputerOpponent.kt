package com.example.board.vs_computer//package com.example.app.board
//
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.io.OutputStreamWriter
//
//fun ComputerMoveToPlay(pgn: String): String {
//    println(pgn)
//    val stockfishPath = "E:\\govind\\6th Sem\\Software Engineering\\App\\App\\src\\main\\java\\com\\example\\app\\board\\stockfish.exe"  // Set the correct path to Stockfish engine
//
//    // Start Stockfish process
//    val processBuilder = ProcessBuilder(stockfishPath)
//    val process = processBuilder.start()
//
//    val writer = OutputStreamWriter(process.outputStream)
//    val reader = BufferedReader(InputStreamReader(process.inputStream))
//
//    // Initialize Stockfish
//    writer.write("uci\n")
//    writer.flush()
//
//    // Set up board position using PGN
////    val formattedMoves = pgn.replace("\n", " ").replace(Regex("""\d+\.""", ""))
////    val formattedMoves = pgn.replace(Regex("""\d+\. """), "")
//    writer.write("position startpos moves $pgn\n")
//    writer.flush()
//
//    // Ask Stockfish to find the best move
//    writer.write("go movetime 2000\n") // Allow 2 seconds for calculation
//    writer.flush()
//
//    // Read and extract best move
//    var bestMove: String? = null
//    var line: String?
//
//    while (reader.readLine().also { line = it } != null) {
//        if (line!!.startsWith("bestmove")) {
//            bestMove = line!!.split(" ")[1] // Extract move (e.g., "e2e4")
//            break
//        }
//    }
//
//    // Close Stockfish process
//    process.destroy()
//
//    // Convert UCI move to "e2 to e4" format
//    return if (bestMove != null && bestMove.length >= 4) {
//        val fromSquare = bestMove.substring(0, 2)
//        val toSquare = bestMove.substring(2, 4)
//        "$fromSquare to $toSquare"
//    } else {
//        "No move found"
//    }
//}