package com.example.service

import com.example.model.User
import com.example.repository.TempUserRepository
import com.example.repository.UserRepository
import java.util.*


class UserService(
    private val UserRepository: UserRepository
) {
    suspend fun findAll(): List<User> = UserRepository.findAll()

    //    fun findById(id: String): User? = userRepository.findById(
//        id = UUID.fromString(id)
//    )
    suspend fun findById(id: String): User? {
        val uuid = try {
            UUID.fromString(id) // Attempt to convert string to UUID
        } catch (e: IllegalArgumentException) {
            return null // Return null if the string is not a valid UUID
        }

        return UserRepository.findById(uuid)
    }


    suspend fun findByEmail(email: String): User? = UserRepository.findByEmail(email)

    suspend fun save(user: User): User? {
        val foundUser = findByEmail(user.email)
        return if (foundUser == null) {
            UserRepository.save(user)
            user
        } else null
    }

}