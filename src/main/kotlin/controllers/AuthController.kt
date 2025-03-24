package com.andrew.greenhouse.auth.controllers

import entities.dto.client.AuthRequest
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

    @PostMapping("/authentication")
    fun authentication(@RequestBody authRequest: AuthRequest) =
        authenticationService.authentication(authRequest)
}