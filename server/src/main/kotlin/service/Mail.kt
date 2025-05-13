package com.example.service


import com.example.routing.request.EmailRequest
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

suspend fun EmailRequest.sendNoReplyEmail() = withContext(Dispatchers.IO) {
    val properties = Properties()
    properties["mail.smtp.auth"] = "true"
    properties["mail.smtp.starttls.enable"] = "true"
    properties["mail.smtp.host"] = "smtp.gmail.com"
    properties["mail.smtp.port"] = "587"

    val email = "ajayjisoni197@gmail.com"
    val password = "vlxq hnsx pmim nloo"
    val personal = "Ajay"

    val session = Session.getInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(email, password)
        }
    })

    val message = MimeMessage(session)
    message.setFrom(InternetAddress(email, personal))
    message.addRecipient(Message.RecipientType.TO, InternetAddress(to))
    message.subject = subject
    message.setText(body)

    Transport.send(message)
}
