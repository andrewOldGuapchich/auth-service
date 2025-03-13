package com.andrew.greenhouse.auth.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import services.MailService
import kotlin.random.Random

@Service
class MailService(
    @Value("\${smtp.mail.name}")
    private val name: String,
    @Autowired private var mailSender: JavaMailSender
) : MailService {
    override fun generateCode(): Number = Random.nextInt(100000, 1000000)

    override fun sendMail(email: String, subject: String, text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val message = SimpleMailMessage().apply {
                from = name
                setTo(email)
                this.subject = subject
                this.text = generateCode().toString()
            }

            mailSender.send(message)
            println("Message is send!")
        }
    }
}
