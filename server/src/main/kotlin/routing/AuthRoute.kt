package com.example.routing

import com.example.repository.UserRepository
import com.example.routing.request.LoginRequest
import com.example.service.JwtService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.authRoute(jwtService: JwtService
,repository: UserRepository) {

    post {
        val loginRequest =
            call.receive<LoginRequest>()

        val token = jwtService.createJwtToken(loginRequest)

        val id = repository.findByEmail(loginRequest.email)?.id.toString()

        token?.let {
            call.respond(hashMapOf("token" to it, "id" to id))
        } ?: call.respond(HttpStatusCode.Unauthorized)
    }

}