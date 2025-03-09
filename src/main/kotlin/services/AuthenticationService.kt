package com.andrew.greenhouse.auth.services

import com.andrew.greenhouse.auth.utils.JwtTokenUtils
import entities.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import services.AuthenticationService
import utils.AuthResponseMessageCode
import utils.RegisterResponseMessageCode

@Service
class AuthenticationService @Autowired constructor(
    private val jwtTokenUtils: JwtTokenUtils,
    private val clientService: ClientService
) : AuthenticationService {
    override fun authentication(authRequest: AuthRequest): ResponseEntity<*> {
        return when (clientService.findClientByLogin(authRequest)) {
            AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE -> ResponseEntity.ok().body(
                AuthResponse(
                    token = jwtTokenUtils.generateAccessToken(authRequest.login),
                    message = AuthResponseMessageCode.SUCCESSFULLY_AUTHENTICATE,
                    status = HttpStatus.OK.value()
                )
            )

            AuthResponseMessageCode.CLIENT_NOT_FOUND -> ResponseEntity.badRequest().body(
                Response(
                    message = AuthResponseMessageCode.CLIENT_NOT_FOUND,
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )

            AuthResponseMessageCode.PASSWORD_INCORRECT -> ResponseEntity.badRequest().body(
                Response(
                    message = AuthResponseMessageCode.PASSWORD_INCORRECT,
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
        }
    }

    override fun registerClientAndTokenGenerate(registerRequest: RegisterRequest): ResponseEntity<*> {
        return when (clientService.registerNewClient(registerRequest)) {
            RegisterResponseMessageCode.ALREADY_EXISTS -> ResponseEntity.badRequest().body(
                Response(
                    message = RegisterResponseMessageCode.ALREADY_EXISTS,
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )

            RegisterResponseMessageCode.SUCCESSFULLY_CREATE -> ResponseEntity.ok(
                RegisterResponse(
                    login = registerRequest.login,
                    token = jwtTokenUtils.generateAccessToken(registerRequest.login),
                    message = RegisterResponseMessageCode.SUCCESSFULLY_CREATE,
                    status = HttpStatus.OK.value()
                )
            )
            RegisterResponseMessageCode.PASSWORD_MATCH_ERROR -> ResponseEntity.badRequest().body(
                Response(
                    message = RegisterResponseMessageCode.PASSWORD_MATCH_ERROR,
                    status = HttpStatus.BAD_REQUEST.value()
                )
            )
        }
    }
}