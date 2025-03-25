package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.utils.JwtTokenUtils
import greenhouse_api.Response
import greenhouse_api.auth_service.entities.dto.client.AuthRequest
import greenhouse_api.auth_service.entities.dto.client.AuthResponse
import greenhouse_api.auth_service.services.AuthenticationService
import greenhouse_api.auth_service.services.ClientServiceL2
import greenhouse_api.utills.AuthResponseMessageCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class AuthenticationService @Autowired constructor(
    private val jwtTokenUtils: JwtTokenUtils,
    private val clientService: ClientServiceL2
) : AuthenticationService {
    override fun authentication(authRequest: AuthRequest): ResponseEntity<*> {
        return when (clientService.findClientByLogin(authRequest)) {
            AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE -> ResponseEntity.ok().body(
                AuthResponse(
                    token = jwtTokenUtils.generateAccessToken(authRequest.login),
                    message = AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE.toString(),
                    status = HttpStatus.OK.value()
                )
            )
            AuthResponseMessageCode.CLIENT_NOT_FOUND -> ResponseEntity.badRequest().body(
                Response(
                    message = AuthResponseMessageCode.CLIENT_NOT_FOUND.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
            AuthResponseMessageCode.PASSWORD_INCORRECT -> ResponseEntity.badRequest().body(
                Response(
                    message = AuthResponseMessageCode.PASSWORD_INCORRECT.toString(),
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
            AuthResponseMessageCode.INTERNAL_ERROR -> ResponseEntity.internalServerError().body(
                Response(
                    message = AuthResponseMessageCode.PASSWORD_INCORRECT.toString(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
            )
        }
    }
}

