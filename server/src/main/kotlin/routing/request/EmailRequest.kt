package com.example.routing.request

import kotlinx.serialization.Serializable
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Pattern

@Serializable
data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String
)

fun verifyEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    if (!Pattern.matches(emailRegex, email)) {
        return false
    }
    val domain = email.substringAfter("@")
    return try {
        InetAddress.getByName(domain)
        true
    } catch (e: UnknownHostException) {
        false
    }
}


fun generateOTP(): String {
    return (100000..999999).random().toString()
}

/*
Android version
suspend fun verifyEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        if (!Pattern.matches(emailRegex, email)) {
            return false
        }

        val domain = email.substringAfter("@")

        return withContext(Dispatchers.IO) {
            try {
                InetAddress.getByName(domain)
                true
            } catch (e: UnknownHostException) {
                false
            }
        }
    }
 */