package com.andrew.greenhouse.auth.services.client_L1

import com.andrew.greenhouse.auth.utils.JwtTokenUtils
import greenhouse_api.Response
import greenhouse_api.auth_service.entities.dto.client.ClientActionRequest
import greenhouse_api.auth_service.entities.dto.client.RegisterRequest
import greenhouse_api.auth_service.entities.dto.client.RegisterResponse
import greenhouse_api.auth_service.entities.dto.client.UpdateRequest
import greenhouse_api.auth_service.services.ClientServiceL1
import greenhouse_api.auth_service.services.ClientServiceL2
import greenhouse_api.utills.ClientActionMessageCode
import greenhouse_api.utills.RegisterResponseMessageCode
import greenhouse_api.utills.UpdateResponseMessageCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class ClientServiceL1 @Autowired constructor(
    private val jwtTokenUtils: JwtTokenUtils,
    private val clientService: ClientServiceL2
): ClientServiceL1 {
    override fun registerClient(registerRequest: RegisterRequest): ResponseEntity<*> {
        return when (clientService.registerNewClient(registerRequest)) {
            RegisterResponseMessageCode.ALREADY_EXISTS -> ResponseEntity.badRequest().body(
                Response(
                    message = RegisterResponseMessageCode.ALREADY_EXISTS.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )

            RegisterResponseMessageCode.PASSWORD_MATCH_ERROR -> ResponseEntity.badRequest().body(
                Response(
                    message = RegisterResponseMessageCode.PASSWORD_MATCH_ERROR.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )

            RegisterResponseMessageCode.WAITING_ACTIVATION_CODE -> ResponseEntity
                .ok(
                    Response(
                        message = RegisterResponseMessageCode.WAITING_ACTIVATION_CODE.toString(),
                        status = HttpStatus.OK.value()
                    )
                )
            RegisterResponseMessageCode.INTERNAL_ERROR -> ResponseEntity.internalServerError().body(
                Response(
                    message = RegisterResponseMessageCode.INTERNAL_ERROR.toString(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
            )
        }
    }

    fun updateClient(updateRequest: UpdateRequest):ResponseEntity<*> {
        return when (clientService.updateClientPassword(updateRequest)) {
            UpdateResponseMessageCode.PASSWORD_MATCH_ERROR -> ResponseEntity.badRequest().body(
                Response(
                    message = UpdateResponseMessageCode.PASSWORD_MATCH_ERROR.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
            UpdateResponseMessageCode.WAITING_VERIFICATION_CODE -> ResponseEntity.ok(
                Response(
                    message = UpdateResponseMessageCode.WAITING_VERIFICATION_CODE.toString(),
                    status = HttpStatus.OK.value()
                )
            )
            UpdateResponseMessageCode.INTERNAL_ERROR -> ResponseEntity.internalServerError().body(
                Response(
                    message = UpdateResponseMessageCode.INTERNAL_ERROR.toString(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
            )
        }
    }

    override fun clientAction(clientActionRequest: ClientActionRequest): ResponseEntity<*> {
        return when (clientService.clientAction(clientActionRequest)) {
            ClientActionMessageCode.CLIENT_NOT_FOUND -> ResponseEntity.badRequest().body(
                Response(
                    message = ClientActionMessageCode.CLIENT_NOT_FOUND.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
            ClientActionMessageCode.CODE_IS_EXPIRED -> ResponseEntity.badRequest().body(
                Response(
                    message = ClientActionMessageCode.CODE_IS_EXPIRED.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
            ClientActionMessageCode.CODE_MATCH_ERROR -> ResponseEntity.badRequest().body(
                Response(
                    message = ClientActionMessageCode.CODE_MATCH_ERROR.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
            ClientActionMessageCode.SUCCESSFULLY_CREATE -> {
                val token = jwtTokenUtils.generateAccessToken(clientActionRequest.login)
                ResponseEntity.ok().body(
                    RegisterResponse(
                        login = clientActionRequest.login,
                        message = ClientActionMessageCode.SUCCESSFULLY_CREATE.toString(),
                        status = HttpStatus.OK.value(),
                        token = token
                    )
                )
            }
            ClientActionMessageCode.SUCCESSFULLY_UPDATE -> ResponseEntity.ok(
                Response(
                    message = ClientActionMessageCode.SUCCESSFULLY_UPDATE.toString(),
                    status = HttpStatus.OK.value(),
                )
            )
            ClientActionMessageCode.INTERNAL_ERROR -> ResponseEntity.internalServerError().body(
                Response(
                    message = ClientActionMessageCode.INTERNAL_ERROR.toString(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
            )
        }
    }

    fun send():ResponseEntity<*> {
        return ResponseEntity.ok().body(
            Response(
                message = "send!",
                status = HttpStatus.OK.value()
            )
        )
    }
}