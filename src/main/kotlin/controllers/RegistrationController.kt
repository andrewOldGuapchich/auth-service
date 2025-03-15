package com.andrew.greenhouse.auth.controllers

import com.andrew.greenhouse.auth.services.RegisterService
import entities.dto.ClientActionRequest
import entities.dto.RegisterRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/registration")
class RegistrationController(
    @Autowired private val registerService: RegisterService
) {
    @PostMapping("/create-client")
    fun registrationNewClient(@RequestBody registerRequest: RegisterRequest): ResponseEntity<*> {
        return registerService.registerClient(registerRequest)
    }

    @PatchMapping("/activation-client/{login}")
    fun activationNewClient(
        @PathVariable login: String,
        @RequestBody clientActionRequest: ClientActionRequest
    ) = registerService.clientAction(clientActionRequest)
}