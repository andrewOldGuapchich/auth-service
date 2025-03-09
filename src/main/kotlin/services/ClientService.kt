package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.repositories.ClientRepositories
import entities.dto.AuthRequest
import entities.dto.MessagePayload
import entities.dto.RegisterRequest
import entities.model.Client
import entities.model.Credential
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import services.ClientService
import utils.AuthResponseMessageCode
import utils.RegisterResponseMessageCode

@Service
class ClientService @Autowired constructor(
    private val clientRepositories: ClientRepositories,
    private val passwordEncoder: PasswordEncoder
) : ClientService {
    override fun findClientByLogin(authRequest: AuthRequest): AuthResponseMessageCode {
        val client = clientRepositories.findByLogin(authRequest.login)
            ?: return AuthResponseMessageCode.CLIENT_NOT_FOUND
        return if (!passwordEncoder.matches(authRequest.password, client.credential?.passwordHash))
            AuthResponseMessageCode.PASSWORD_INCORRECT
        else AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE
    }

    override fun registerNewClient(request: RegisterRequest): RegisterResponseMessageCode =
        clientRepositories.findByLogin(request.login)?.let {
            return@let RegisterResponseMessageCode.ALREADY_EXISTS
        } ?: run {
            val client = Client(
                login = request.login,
                emailAddress = request.email,
                messagePayload = MessagePayload(request).toString()
            )

            val credential = Credential(
                passwordHash = passwordEncoder.encode(request.password),
                client = client
            )

            client.credential = credential
            save(client)
            RegisterResponseMessageCode.SUCCESSFULLY_CREATE
        }

    @Transactional
    private fun save(client: Client) = clientRepositories.save(client)
}