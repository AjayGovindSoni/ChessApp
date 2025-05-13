package com.example.repository

import com.example.model.otp_requests
import java.util.UUID

interface otp_requestReposiratory {
    suspend fun findByEmail(email: String):otp_requests?
    suspend fun save(otpRequests: otp_requests):Boolean
    suspend fun delete()
    suspend fun remove(email: String)
}