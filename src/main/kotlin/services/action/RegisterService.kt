package com.andrew.greenhouse.auth.services.action

import com.andrew.greenhouse.auth.services.client.ClientService
import com.andrew.greenhouse.auth.utils.JwtTokenUtils
import entities.dto.*
import entities.dto.client.ClientActionRequest
import entities.dto.client.RegisterRequest
import entities.dto.client.RegisterResponse
import entities.dto.client.UpdateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import services.RegisterService
import utils.ClientActionMessageCode
import utils.RegisterResponseMessageCode
import utils.UpdateResponseMessageCode

@Service
class RegisterService @Autowired constructor(
    private val jwtTokenUtils: JwtTokenUtils,
    private val clientService: ClientService
): RegisterService{
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
            ClientActionMessageCode.SUCCESSFULLY_UPDATE -> ResponseEntity.ok("sdfgdg")
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