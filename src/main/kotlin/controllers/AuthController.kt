package com.andrew.greenhouse.auth.controllers

import greenhouse_api.auth_service.entities.dto.client.AuthRequest
import greenhouse_api.auth_service.services.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    @Autowired private val authenticationService: AuthenticationService
) {

    @PostMapping("/authentication")
    fun authentication(@RequestBody authRequest: AuthRequest) =
        authenticationService.authentication(authRequest)
}