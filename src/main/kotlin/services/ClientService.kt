package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.repositories.ClientRepositories
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import entities.dto.*
import entities.model.*
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import services.ClientService
import utils.*
import java.time.LocalDateTime

@Service
class ClientService @Autowired constructor(
    private val clientRepositories: ClientRepositories,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val verificationService: VerificationService
) : ClientService {
    override fun clientAction(clientActionRequest: ClientActionRequest): ClientActionMessageCode {
        val client = findClientByLogin(clientActionRequest.login)
            ?: return ClientActionMessageCode.CLIENT_NOT_FOUND
        val verificationData = client.getActiveVerificationData()
            ?: return ClientActionMessageCode.CODE_IS_EXPIRED
        return if (verificationData.verifyCode != clientActionRequest.verifyCode)
            ClientActionMessageCode.CODE_MATCH_ERROR
        else {
            when (clientActionRequest.action) {
                ClientAction.CREATE -> {
                    val createdClient = Client(
                        amndState = AmndState.ACTIVE,
                        login = client.login,
                        emailAddress = client.emailAddress,
                        messagePayload = client.messagePayload,
                        action = ClientAction.CREATE,
                        prevClient = client
                    )
                    createdClient.credentials = client.credentials
                        .map { cred ->
                            Credential(
                                passwordHash = cred.passwordHash,
                                client = createdClient
                            )
                        }.toMutableList()

                    save(createdClient)
                    ClientActionMessageCode.SUCCESSFULLY_CREATE
                }
                ClientAction.UPDATE -> {
                    ClientActionMessageCode.SUCCESSFULLY_UPDATE
                }
                ClientAction.DELETE -> {
                    ClientActionMessageCode.SUCCESSFULLY_DELETE
                }
            }
        }
        //return ClientActionMessageCode.CODE_IS_EXPIRED
    }

    override fun findClientByLogin(authRequest: AuthRequest): AuthResponseMessageCode {
        val client = clientRepositories.findByLogin(authRequest.login)
            ?: return AuthResponseMessageCode.CLIENT_NOT_FOUND
        return if (!passwordEncoder.matches(authRequest.password, client.credentials.single().passwordHash))
            AuthResponseMessageCode.PASSWORD_INCORRECT
        else AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE
    }

    fun findClientByLogin(login: String): Client? {
        return clientRepositories.findByLogin(login)
    }

    override fun registerNewClient(registerRequest: RegisterRequest): RegisterResponseMessageCode {
        if (listOf(
            clientRepositories.findByLogin(registerRequest.login),
            clientRepositories.findByEmail(registerRequest.email)
        ).any { it != null }
        ) return RegisterResponseMessageCode.ALREADY_EXISTS

        if (registerRequest.password != registerRequest.passwordConfirm) {
            return RegisterResponseMessageCode.PASSWORD_MATCH_ERROR
        }

        val verificationCode = verificationService.generateVerificationCode()

        val client = Client(
            amndState = AmndState.WAITING,
            login = registerRequest.login,
            emailAddress = registerRequest.email,
            messagePayload = createMessagePayload(registerRequest),
            action = ClientAction.CREATE
        ).apply {
            credentials = mutableListOf(Credential(
                passwordHash = passwordEncoder.encode(registerRequest.password),
                client = this
            ))
            verificationData = mutableListOf(VerificationData(
                client = this,
                clientEmail = registerRequest.email,
                amndState = AmndState.ACTIVE,
                verifyCode = verificationCode.toString(),
                createDate = LocalDateTime.now(),
                expireDate = LocalDateTime.now().plusMinutes(10)
            ))
        }

//        client = save(client)
//        client = client.copy(prevClient = client)
        save(client)

        // Отправка письма асинхронно
        val messageText = emailService.generateMessage(client.action, verificationCode)
        emailService.sendMail(registerRequest.email, messageText)

        return RegisterResponseMessageCode.WAITING_ACTIVATION_CODE
    }


    override fun updateClientPassword(updateRequest: UpdateRequest): UpdateResponseMessageCode {
        return UpdateResponseMessageCode.PASSWORD_MATCH_ERROR
    }

    @Transactional
    private fun save(client: Client) = clientRepositories.save(client)

    private fun createMessagePayload(request: RegisterRequest):String {
        val maskedRequest = request.copy(password = "****", passwordConfirm = "*****")
        return jacksonObjectMapper().writeValueAsString(maskedRequest)
    }
}
