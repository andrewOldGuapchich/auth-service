package com.andrew.greenhouse.auth.services.client_L2

import com.andrew.greenhouse.auth.repositories.ClientRepositories
import com.andrew.greenhouse.auth.services.kafka.ProducerService
import com.andrew.greenhouse.auth.utils.RestHandler
import com.andrew.greenhouse.auth.utils.Topic
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import greenhouse_api.MessagePayload
import greenhouse_api.auth_service.entities.builder.ClientBuilder
import greenhouse_api.auth_service.entities.builder.ClientParams
import greenhouse_api.auth_service.entities.builder.CredentialParams
import greenhouse_api.auth_service.entities.dto.client.AuthRequest
import greenhouse_api.auth_service.entities.dto.client.ClientActionRequest
import greenhouse_api.auth_service.entities.dto.client.RegisterRequest
import greenhouse_api.auth_service.entities.dto.client.UpdateRequest
import greenhouse_api.auth_service.entities.model.AmndState
import greenhouse_api.auth_service.entities.model.Client
import greenhouse_api.auth_service.entities.model.ClientAction
import greenhouse_api.auth_service.entities.model.Credential
import greenhouse_api.auth_service.services.ClientServiceL2
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import greenhouse_api.kafka_messages.ClientNtfActionStreaming
import greenhouse_api.utills.AuthResponseMessageCode
import greenhouse_api.utills.ClientActionMessageCode
import greenhouse_api.utills.RegisterResponseMessageCode
import greenhouse_api.utills.UpdateResponseMessageCode

@Service
class ClientServiceL2 @Autowired constructor(
    private val clientRepositories: ClientRepositories,
    private val passwordEncoder: PasswordEncoder,
    private val producerService: ProducerService,
    private val restHandler: RestHandler
) : ClientServiceL2 {
    private val logger = LoggerFactory.getLogger(ClientServiceL2::class.java)
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
                        val activeClient = createClient {
                            amndState = AmndState.WAITING
                            login = prevClient.login
                            emailAddress = prevClient.emailAddress
                            messagePayload = prevClient.messagePayload
                            action = ClientAction.CREATE
                        }
                        save(activeClient)
                        ClientActionMessageCode.SUCCESSFULLY_CREATE
                    }

                    ClientAction.UPDATE -> {
                        prevClient.amndState = AmndState.ACTIVE
                        prevClient.credentials.first().amndState = AmndState.ACTIVE

                        save(prevClient)
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

    override fun createClient(clientBuilder: ClientBuilder): Client {
        val clientParams = ClientParams().apply(clientBuilder)
        return Client(
            amndState = clientParams.amndState,
            login = clientParams.login,
            emailAddress = clientParams.emailAddress,
            messagePayload = clientParams.messagePayload,
            action = ClientAction.CREATE
        ).apply {
            clientParams.credentials?.let { cred ->
                val credentialParams = CredentialParams().apply(cred)
                credentials.add(
                    Credential(
                        passwordHash = credentialParams.passwordHash,
                        client = this
                    )
                )
            }
        }
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

            val client = createClient {
                amndState = AmndState.WAITING
                login = registerRequest.login
                emailAddress = registerRequest.email
                messagePayload = createMessagePayload(registerRequest)
                action = ClientAction.CREATE
                credentials = {
                    passwordHash = passwordEncoder.encode(registerRequest.password)
                }
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

        val updateClient = createClient {
            amndState = AmndState.WAITING
            login = oldClient.login
            emailAddress = oldClient.emailAddress
            messagePayload = oldClient.messagePayload
            action = ClientAction.UPDATE
            credentials = {
                passwordHash = passwordEncoder.encode(updateRequest.newPassword)
            }
        }

        createKafkaMessage(updateRequest)?.let {
            producerService.sendMessage(Topic.OTP_TOPIC_OUTGOING, it)
        }
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
            )
        )
    }

    private fun getOtp(login: String) = restHandler.get {
        endpoint = "/smart-greenhouse-ntf/get-otp"
        params = mutableMapOf("login" to login)
        port = 20103
        bodyClass = OtpClientDto::class.java
    }

    private fun createMessagePayload(request: RegisterRequest):String {
        val maskedRequest = request.copy(password = "*****", passwordConfirm = "*****")
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
                    mail = request.email,
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

    @Transactional
    private fun save(client: Client) = clientRepositories.save(client)
}

//вынести в ntf-api
data class OtpClientDto(
    val login: String,
    val otp: String?,
)