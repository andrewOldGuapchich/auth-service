package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.repositories.VerifyDataRepository
import entities.model.AmndState
import entities.model.Client
import entities.model.VerificationData
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import services.VerificationService
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class VerificationService @Autowired constructor(
    private val verifyDataRepository: VerifyDataRepository
) : VerificationService{
    //должен отдавать сообщение или код resolveVerificationCode
    override fun findVerificationCode(client: Client): VerificationData {
        val verificationData = verifyDataRepository.findByClient(client.id).firstOrNull()

        TODO("Not yet implemented")
    }

    override fun generateVerificationCode(): Number = Random.nextInt(100000, 1000000)

    override fun saveVerificationCode(client: Client): Number {
        val code = generateVerificationCode()
        val verificationData =
            VerificationData(
                client = client,
                clientEmail = client.emailAddress,
                amndState = AmndState.ACTIVE,
                verifyCode = code.toString(),
                createDate = LocalDateTime.now(),
                expireDate = LocalDateTime.now().plusMinutes(10)
            )
        save(verificationData)
        return code
    }

    @Transactional
    private fun save(verificationData: VerificationData): VerificationData =
        verifyDataRepository.save(verificationData)
}