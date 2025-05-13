package com.example.repository

import com.example.model.User
import java.util.UUID

interface UserRepository {
    suspend fun findAll():List<User>
    suspend fun findById(id:UUID):User?
    suspend fun findByEmail(email: String):User?
    suspend fun save(user: User):UUID?
    suspend fun updatePassword(email: String,password: String):Boolean
}