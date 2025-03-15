package com.andrew.greenhouse.auth.services

import entities.model.Client
import entities.model.ClientAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import services.EmailService
import kotlin.random.Random

@Service
class EmailService(
    @Value("\${smtp.mail.name}")
    private val name: String,
    @Autowired private var mailSender: JavaMailSender
) : EmailService {

    //override fun generateCode(): Number = Random.nextInt(100000, 1000000)

    override fun sendMail(email: String, text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val message = SimpleMailMessage().apply {
                from = email
                setTo(email)
                this.subject = "Greenhouse verification"
                this.text = text
            }
            mailSender.send(message)
        }
    }

    override fun generateMessage(clientAction: ClientAction, code: Number): String {
        return when (clientAction) {
            ClientAction.CREATE -> "Регистрация пользователя системы \"Smart Greenhouse\".\nКод активации учетной записи: $code"
            ClientAction.UPDATE -> "Обновление пользовательских данных.\nКод для обновления: $code"
            ClientAction.DELETE -> "Удаление учетной записи.\nКод для удаления учетной записи: $code"
        }
    }
}
