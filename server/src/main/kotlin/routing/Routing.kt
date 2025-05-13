package com.example.routing

import com.example.routing.request.EmailRequest
import com.example.repository.TempUserRepository
import com.example.repository.UserRepository
import com.example.service.JwtService
import com.example.service.sendNoReplyEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userRepository:UserRepository,
    jwtService: JwtService,
    tempRepo: TempUserRepository
) {
    routing {

        route("api/auth"){
            authRoute(jwtService, userRepository)
        }

        route("/api/user"){
            UserRoute(userRepository,tempRepo)
        }

        post("/send-email") {
            val emailRequest = call.receive<EmailRequest>()

            try {
                emailRequest.sendNoReplyEmail()
                call.respond(HttpStatusCode.OK, "Email sent successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "Failed to send email")
            }
        }
    }
}
