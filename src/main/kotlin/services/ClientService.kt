package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.repositories.ClientRepositories
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import entities.dto.AuthRequest
import entities.dto.ClientActionRequest
import entities.dto.RegisterRequest
import entities.dto.UpdateRequest
import entities.model.AmndState
import entities.model.Client
import entities.model.ClientAction
import entities.model.Credential
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import services.ClientService
import utils.*

@Service
class ClientService @Autowired constructor(
    private val clientRepositories: ClientRepositories,
    private val passwordEncoder: PasswordEncoder,
    private val mailService: MailService
) : ClientService {
    override fun clientAction(clientActionRequest: ClientActionRequest): ClientActionMessageCode {
        TODO("Not yet implemented")
    }

    override fun findClientByLogin(authRequest: AuthRequest): AuthResponseMessageCode {
        val client = clientRepositories.findByLogin(authRequest.login)
            ?: return AuthResponseMessageCode.CLIENT_NOT_FOUND
        return if (!passwordEncoder.matches(authRequest.password, client.credential?.passwordHash))
            AuthResponseMessageCode.PASSWORD_INCORRECT
        else AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE
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

        val client = Client(
            amndState = AmndState.WAITING,
            login = registerRequest.login,
            emailAddress = registerRequest.email,
            messagePayload = createMessagePayload(registerRequest),
            action = ClientAction.CREATE
        ).apply {
            credential = Credential(
                passwordHash = passwordEncoder.encode(registerRequest.password),
                client = this
            )
        }

        save(client)

        // Отправка письма асинхронно
        mailService.sendMail(registerRequest.email, "Activation code", "sgsg")
        return RegisterResponseMessageCode.WAITING_ACTIVATION_CODE
    }


    override fun updateClientPassword(updateRequest: UpdateRequest): UpdateResponseMessageCode {
        TODO("Not yet implemented")
    }

    @Transactional
    private fun save(client: Client) = clientRepositories.save(client)

    private fun createMessagePayload(request: RegisterRequest):String {
        val maskedRequest = request.copy(password = "****", passwordConfirm = "*****")
        return jacksonObjectMapper().writeValueAsString(maskedRequest)
    }
}