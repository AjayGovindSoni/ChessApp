package com.example.db

import com.example.model.ChatMessage
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.select
import com.example.model.User
import com.example.model.otp_requests
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import java.util.*
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object UserTable : UUIDTable("User") {  // ✅ Use UUIDTable instead of IdTable
    val email = varchar("email", 50)
    val password = varchar("password", 50)
}

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var email by UserTable.email
    var password by UserTable.password
}

suspend fun <T> suspendTransaction(block: Transaction.()->T):T=
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: UserDAO) = User(
    id = dao.id.value,
    email = dao.email,
    password = dao.password
)

object OtpRequestTable : UUIDTable("otp_requests") {
    val email = text("email")
    val otp = text("otp")
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")
}

class OtpDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<OtpDAO>(OtpRequestTable)

    var email by OtpRequestTable.email
    var otp by OtpRequestTable.otp
}


fun OTPDaoToModel(dao: OtpDAO) = otp_requests(
    email = dao.email,
    otp = dao.otp
)

object ChatMessageTable : UUIDTable("ChatMessage") {
    val sender = reference("sender", UserTable)
    val content = text("content")
    val timestamp = long("timestamp") // Epoch milliseconds
}

class ChatMessageDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ChatMessageDAO>(ChatMessageTable)

    var sender by UserDAO referencedOn ChatMessageTable.sender
    var content by ChatMessageTable.content
    var timestamp by ChatMessageTable.timestamp
}

fun daoToModel(dao: ChatMessageDAO) = ChatMessage(
    id = dao.id.value,
    senderId = dao.sender.id.value,
    content = dao.content,
    timestamp = dao.timestamp
)




/*
manually set id as:
val newUser = UserDAO.new(user.id) {  // ✅ Manually setting UUID
    email = user.email
    password = user.password
}
 */