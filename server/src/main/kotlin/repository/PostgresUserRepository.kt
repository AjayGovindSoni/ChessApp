package com.example.repository

import com.example.db.UserDAO
import com.example.db.UserTable
import com.example.db.daoToModel
import com.example.db.suspendTransaction
import com.example.model.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PostgresUserRepository:UserRepository {
    override suspend fun findAll(): List<User> = suspendTransaction {
        UserDAO.all().map(::daoToModel)
    }

    override suspend fun findById(id: UUID): User? = suspendTransaction {
        UserDAO
            .find{(UserTable.id eq id)}
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun findByEmail(email: String): User?  = suspendTransaction {
        UserDAO
            .find{(UserTable.email eq email)}
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun save(user: User):UUID?{
        try {
            suspendTransaction {
                UserDAO.new(user.id) {
                    email = user.email
//                    password = PasswordHasher.hash(user.password)
                    password = user.password
                }
            }
        }catch (ex: Exception){
            return null
        }
        return user.id
    }

    override suspend fun updatePassword(email: String, password: String): Boolean {
        return transaction {
            val user = UserDAO.find { UserTable.email eq email }
                .forUpdate()
                .limit(1)
                .firstOrNull()

            if (user != null) {
//                user.password = PasswordHasher.hash(password)
                user.password = password
                true
            } else {
                false
            }
        }
    }

    object PasswordHasher {
        fun hash(password: String): String {
            return at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, password.toCharArray())
        }

        fun verify(password: String, hash: String): Boolean {
            return at.favre.lib.crypto.bcrypt.BCrypt.verifyer().verify(password.toCharArray(), hash).verified
        }
    }

}