package com.andrew.greenhouse.auth.controllers

import entities.dto.AuthRequest
import entities.dto.RegisterRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import services.AuthenticationService

@RestController
@RequestMapping("/auth")
class AuthController(
    @Autowired private val authenticationService: AuthenticationService
) {
    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest) =
        authenticationService.registerClientAndTokenGenerate(registerRequest)

    @PostMapping("/authentication")
    fun authentication(@RequestBody authRequest: AuthRequest) =
        authenticationService.authentication(authRequest)
}