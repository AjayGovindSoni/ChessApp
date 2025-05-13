package com.example

import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import com.example.repository.PostgresUserRepository
import com.example.repository.TempUserRepository
import com.example.routing.configureRouting
import com.example.service.JwtService
import com.example.service.UserService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val userRepository = PostgresUserRepository()
    val userService = UserService(userRepository)
    val jwtService = JwtService(this, userService)
    val tempRepo = TempUserRepository()


    configureSecurity(jwtService)
    configureSerialization()
    configureDatabases()
    configureMonitoring()
    configureSockets()
    configureRouting(userRepository, jwtService,tempRepo)
}
