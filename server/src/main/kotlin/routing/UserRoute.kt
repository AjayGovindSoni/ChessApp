package com.example.routing

import com.example.db.OtpRequestTable
import com.example.model.User
import com.example.repository.TempUserRepository
import com.example.repository.UserRepository
import com.example.routing.request.*
import com.example.routing.response.UserResponse
import com.example.service.sendNoReplyEmail
import getReview
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Route.UserRoute(
    repository: UserRepository,
    tempRepo: TempUserRepository
) {

    val otpStore = mutableMapOf<String, String>()

    post {
        val userRequest = call.receive<UserRequest>()

        if(repository.findByEmail(userRequest.email)!=null){
            call.respond(
                HttpStatusCode.Conflict,
                mapOf("message" to "A user with this email already exists.")
            )
            return@post
        }

        val verifyEmail = verifyEmail(userRequest.email)

        if (verifyEmail == false) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        val otp = generateOTP()

        insert_otp(userRequest.email,otp)

        val emailRequest = EmailRequest(
            to = userRequest.email, subject = "OTP VERIFICATION", body = """
            
            Your otp is ${otp}.
            It is valid for 30 minutes only.
            
        """.trimIndent()
        )

        val user = userRequest.toModel()

        val isTempUserCreated = tempRepo.save(user)

        val tempUser = tempRepo.findByEmail(userRequest.email)

        try {
            emailRequest.sendNoReplyEmail()
            call.respond(HttpStatusCode.OK, "Email sent successfully")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, "Failed to send email")
        }

        post("/otp/{receivedOtp}") {

            val receivedOtp: String =
                call.parameters["receivedOtp"] ?: return@post call.respond(HttpStatusCode.BadRequest)
//
//            val expectedOtp = find_otp(user.email)
//            println(expectedOtp)

            if (!receivedOtp.equals(otp)) return@post call.respond(HttpStatusCode.BadRequest)

            val createdUserId = tempUser?.let {
                repository.save(
                    it
                )
            } ?: return@post call.respond(HttpStatusCode.BadRequest)

            call.response.header(
                name = "id", value = createdUserId.toString()
            )

            call.respond(
                message = HttpStatusCode.Created
            )
        }
    }

    post("/reset-password-request") {
        val request = call.receive<ResetPassword>()
        val isEmailValid = verifyEmail(request.email)

        if (!isEmailValid) return@post call.respond(HttpStatusCode.BadRequest)

        val otp = generateOTP()
        otpStore[request.email] = otp // store otp temporarily

        val emailRequest = EmailRequest(
            to = request.email, subject = "OTP VERIFICATION", body = """
            Your otp is $otp.
            It is valid for 30 minutes only.
        """.trimIndent()
        )

        println("Sending OTP: $otp")

        try {
            emailRequest.sendNoReplyEmail()
            call.respond(HttpStatusCode.OK, "Email sent successfully")
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, "Failed to send email")
        }
    }

    post("/verify-reset-otp/{email}/{otp}") {
        val email = call.parameters["email"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val receivedOtp = call.parameters["otp"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        val expectedOtp = otpStore[email]

        println("Verifying: received=$receivedOtp, expected=$expectedOtp")

        if (expectedOtp == null || expectedOtp != receivedOtp) {
            return@post call.respond(HttpStatusCode.BadRequest)
        }

        call.respond(HttpStatusCode.OK)
    }

    post("/reset-password") {
        val request = call.receive<SetNewPassword>()
        val email = request.email
        val newPassword = request.newPassword

        val isPasswordUpdated = repository.updatePassword(email, newPassword)
        if (isPasswordUpdated) {
            otpStore.remove(email) // Optional: clear OTP after success
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }


    authenticate {
        get {
            val users = repository.findAll()

            call.respond(message = users.map { it.toResponse() })
        }

        get("/gameReview/{pgn}") {
            val pgn: String = call.parameters["pgn"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val gameReview = getReview(pgn)

            if (gameReview == null) return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(message = gameReview)
        }

        post("/gameReview") {

            val reviewRequest = call.receive<GameReviewRequest>()

            val pgn = reviewRequest.pgn

            val gameReview = getReview(pgn)

            if (gameReview == null) return@post call.respond(HttpStatusCode.BadRequest)
            call.respond(message = gameReview)
        }
    }


    authenticate("another-auth") {
        get("/{id}") {
            val id: String = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val uuid = UUID.fromString(id)

            val foundUser = repository.findById(uuid)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            if (foundUser.email != extractPrincipalEmail(call))
                return@get call.respond(HttpStatusCode.NotFound)

            call.respond(
                message = foundUser.toResponse()
            )
        }
    }
}

fun extractPrincipalEmail(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("email")
        ?.asString()


private fun UserRequest.toModel(): User = User(
    id = UUID.randomUUID(),
    email = this.email,
    password = this.password,
)

private fun User.toResponse(): UserResponse = UserResponse(
    id = this.id, email = this.email
)

object OtpRequests : Table("otp_requests") {
    val email = varchar("email", 255)
    val otp = varchar("otp", 6)
    val createdAt = datetime("created_at")
    val expiresAt = datetime("expires_at")
}

fun insert_otp(emailValue: String, otpValue: String){
    transaction {
        OtpRequests.insert {
            it[email] = emailValue
            it[otp] = otpValue
        }
    }
}

fun remove_otp(emailValue: String){
    transaction {
        OtpRequests.deleteWhere { OtpRequests.email eq email }
    }
}

fun delete() {
    transaction {
        OtpRequests.deleteWhere {
            expiresAt less CurrentDateTime
        }
    }
}

fun find_otp(emailValue: String):String{
    return transaction {
        val query = OtpRequests.select(OtpRequests.email eq emailValue)
        println(query)
        val storedotp = query.firstOrNull()?.get(OtpRequests.otp)
        println("OTP = $storedotp")
        storedotp ?: ""
    }
}