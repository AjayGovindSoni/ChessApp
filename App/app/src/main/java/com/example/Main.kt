//package com.example
//
//import com.example.api.*
//import kotlinx.coroutines.runBlocking
//
//fun main(): kotlin.Unit = runBlocking {
//    registerUser("someUsername", "pwd")
//
//    val isLoggedIn = login("goviaj.2004@gmail.com", "pwd")
//
//    if (isLoggedIn) {
//        val userData = getUser()
//        println("User Data: $userData")
//
//        val userId = "80e5b086-fb7d-4ea2-b9c1-2014ae9c365f"
//        val userById = getUserById(userId)
//        println("User by ID: $userById")
//    }

//    val pgn = """
//            [Event "Example Game"]
//            [Site "Chess.com"]
//            [Date "2023.01.15"]
//            [Round "1"]
//            [White "Player1"]
//            [Black "Player2"]
//            [Result "1-0"]
//
//            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Na5 10. Bc2 c5
//            11. d4 Qc7 12. Nbd2 cxd4 13. cxd4 exd4 14. Nxd4 Nc6 15. Nxc6 Qxc6 16. e5 dxe5 17. Rxe5 Bd6 18. Re1 Bd7
//            19. Nf3 Rae8 20. Rxe8 Rxe8 21. Bg5 Be6 22. Qd4 h6 23. Bxf6 gxf6 24. Qxf6 Bh2+ 25. Kh1 Bf4 26. g3 Be5
//            27. Qf5 Bxg3 28. fxg3 Qxc2 29. Qxe6 Qc4 30. Re1
//        """
//    val gameReviewRequest = GameReviewRequest(pgn = pgn)
//
//    val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJqd3QtYXVkaWVuY2UiLCJpc3MiOiJ5b3VyLWlzc3VlciIsImVtYWlsIjoiZ292aWFqLjIwMDRAZ21haWwuY29tIiwiZXhwIjoxNzQzODUxOTkxfQ.-V8mGBg2ZRFX0HYGnregdmZQg2ZmtJgJgJo96o7uacA"
//    reviewGame(gameReviewRequest, token)
//}