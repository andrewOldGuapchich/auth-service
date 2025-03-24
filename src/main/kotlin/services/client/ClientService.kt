package com.andrew.greenhouse.auth.services.client

import MessagePayload
import com.andrew.greenhouse.auth.repositories.ClientRepositories
import com.andrew.greenhouse.auth.services.OtpService
import com.andrew.greenhouse.auth.services.kafka.ProducerService
import com.andrew.greenhouse.auth.utils.RestHandler
import com.andrew.greenhouse.auth.utils.Topic
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import entities.dto.client.AuthRequest
import entities.dto.client.ClientActionRequest
import entities.dto.client.RegisterRequest
import entities.dto.client.UpdateRequest
import entities.dto.stream.ClientNtfActionStreaming
import entities.model.*
import jakarta.transaction.Transactional
import org.hibernate.sql.Update
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import services.ClientService
import utils.*
import kotlin.math.log

@Service
class ClientService @Autowired constructor(
    private val clientRepositories: ClientRepositories,
    private val passwordEncoder: PasswordEncoder,
    private val otpService: OtpService,
    private val producerService: ProducerService,
    private val restHandler: RestHandler
) : ClientService {
    private val logger = LoggerFactory.getLogger(ClientService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)


    override fun clientAction(clientActionRequest: ClientActionRequest): ClientActionMessageCode {
        try {
            val prevClient = clientRepositories.findWaitingClient(clientActionRequest.login)
                ?: return ClientActionMessageCode.CLIENT_NOT_FOUND

            val otp = getOtp(clientActionRequest.login)?.otp
                ?: return ClientActionMessageCode.CODE_IS_EXPIRED
            return if (otp != clientActionRequest.verifyCode)
                ClientActionMessageCode.CODE_MATCH_ERROR
            else {
                when (clientActionRequest.action) {
                    ClientAction.CREATE -> {
                        val activeClient = Client(
                            amndState = AmndState.ACTIVE,
                            login = prevClient.login,
                            emailAddress = prevClient.emailAddress,
                            messagePayload = prevClient.messagePayload,
                            action = ClientAction.CREATE,
                            prevClient = prevClient
                        )
                        activeClient.verificationData = mutableListOf(
                            OtpArchive(
                                client = activeClient,
                                createDate = prevClient.amndDate,
                                expireDate = prevClient.amndDate?.plusMinutes(10),
                                amndState = AmndState.INACTIVE,
                                clientEmail = activeClient.emailAddress,
                                verifyCode = clientActionRequest.verifyCode,
                                action = ClientAction.CREATE
                            )
                        )
                        otpService.deleteOtp(activeClient.login)

                        save(activeClient)
                        ClientActionMessageCode.SUCCESSFULLY_CREATE
                    }

                    ClientAction.UPDATE -> {
                        ClientActionMessageCode.SUCCESSFULLY_UPDATE
                    }

                    ClientAction.DELETE -> {
                        ClientActionMessageCode.INTERNAL_ERROR
                    }
                }
            }
        } catch (e: Exception){
            logger.error("Internal server error! ${e.message}")
            return ClientActionMessageCode.INTERNAL_ERROR
        }
    }

    override fun findClientByLogin(authRequest: AuthRequest): AuthResponseMessageCode {
        val client = clientRepositories.findByLogin(authRequest.login)
            ?: return AuthResponseMessageCode.CLIENT_NOT_FOUND
        return if (!passwordEncoder.matches(authRequest.password, client.credentials.single().passwordHash))
            AuthResponseMessageCode.PASSWORD_INCORRECT
        else AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE
    }

    override fun registerNewClient(registerRequest: RegisterRequest): RegisterResponseMessageCode {
        try {
            if (listOf(
                    clientRepositories.findByLogin(registerRequest.login),
                    clientRepositories.findByEmail(registerRequest.email)
                ).any { it != null }
            ) return RegisterResponseMessageCode.ALREADY_EXISTS

            if (registerRequest.password != registerRequest.passwordConfirm)
                return RegisterResponseMessageCode.PASSWORD_MATCH_ERROR

            val client = Client(
                amndState = AmndState.WAITING,
                login = registerRequest.login,
                emailAddress = registerRequest.email,
                messagePayload = createMessagePayload(registerRequest),
                action = ClientAction.CREATE
            ).apply {
                credentials = mutableListOf(
                    Credential(
                        passwordHash = passwordEncoder.encode(registerRequest.password),
                        client = this
                    )
                )
            }
            save(client)
            createKafkaMessage(registerRequest)?.let {
                producerService.sendMessage(Topic.OTP_TOPIC_OUTGOING, it)
            }
            return RegisterResponseMessageCode.WAITING_ACTIVATION_CODE
        } catch (e: Exception){
            logger.info("Client register - ${e.message}")
            return RegisterResponseMessageCode.INTERNAL_ERROR
        }
    }

    override fun updateClientPassword(updateRequest: UpdateRequest): UpdateResponseMessageCode {
        val oldClient = clientRepositories.findByLogin(updateRequest.login)
            ?: return UpdateResponseMessageCode.PASSWORD_MATCH_ERROR
        val updateClient = Client(
            amndState = AmndState.ACTIVE,
            login = oldClient.login,
            emailAddress = oldClient.emailAddress,
            messagePayload = oldClient.messagePayload,
            prevClient = oldClient,
            action = ClientAction.UPDATE
        ).apply {
            credentials = mutableListOf(
                Credential(
                    passwordHash = passwordEncoder.encode(updateRequest.newPassword),
                    client = this
                )
            )
        }
        oldClient.amndState = AmndState.INACTIVE
        clientRepositories.save(updateClient)
        return UpdateResponseMessageCode.WAITING_VERIFICATION_CODE
    }

    fun deleteClient(clientActionRequest: ClientActionRequest, prevClient: Client): Client {
        return clientRepositories.save(
            Client(
                amndState = AmndState.CLOSED,
                prevClient = prevClient,
                action = ClientAction.DELETE,
                login = prevClient.login,
                emailAddress = prevClient.emailAddress
            ).apply {
                verificationData = mutableListOf(
                    OtpArchive(
                        client = this,
                        createDate = prevClient.amndDate,
                        expireDate = prevClient.amndDate?.plusMinutes(10),
                        amndState = AmndState.INACTIVE,
                        clientEmail = this.emailAddress,
                        verifyCode = clientActionRequest.verifyCode,
                        action = ClientAction.CREATE
                    )
                )
            }
        )
    }

    @Transactional
    private fun save(client: Client) = clientRepositories.save(client)

    fun getOtp(login: String) = restHandler.get {
        endpoint = "/smart-greenhouse-ntf/get-otp"
        params = mutableMapOf("login" to login)
        port = 20103
        bodyClass = OtpClientDto::class.java
    }


    private fun createMessagePayload(request: RegisterRequest):String {
        val maskedRequest = request.copy(password = "****", passwordConfirm = "*****")
        return objectMapper.writeValueAsString(MessagePayload(request = maskedRequest))
    }

    private fun<T> createKafkaMessage(request: T):String? {
        val payload = when(request) {
            is RegisterRequest -> MessagePayload(
                request = ClientNtfActionStreaming(
                    login = request.login,
                    mail = request.email,
                    action = ClientAction.CREATE
                )
            )
            is UpdateRequest -> MessagePayload(
                request = ClientNtfActionStreaming(
                    login = request.login,
                    mail = "",
                    action = ClientAction.UPDATE
                )
            )
            else -> return null
        }

        return try {
            objectMapper.writeValueAsString(payload)
        } catch (e: Exception) {
            logger.error("Error serializing Kafka message: ${e.message}")
            null
        }
    }
}

data class OtpClientDto(
    val login: String,
    val otp: String?,
)