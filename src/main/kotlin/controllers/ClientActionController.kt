package com.andrew.greenhouse.auth.controllers

import com.andrew.greenhouse.auth.services.client_L1.ClientServiceL1
import greenhouse_api.auth_service.entities.dto.client.ClientActionRequest
import greenhouse_api.auth_service.entities.dto.client.RegisterRequest
import greenhouse_api.auth_service.entities.dto.client.UpdateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ClientActionController(
    @Autowired private val clientService: ClientServiceL1
) {
    @PostMapping("/create-client")
    fun registrationNewClient(@RequestBody registerRequest: RegisterRequest): ResponseEntity<*> {
        return clientService.registerClient(registerRequest)
    }

    @PatchMapping("/activation-client/{login}")
    fun activationNewClient(
        @PathVariable login: String,
        @RequestBody clientActionRequest: ClientActionRequest
    ) = clientService.clientAction(clientActionRequest)

    @PatchMapping("/update-client")
    fun updatePassword(
        @RequestBody updateRequest: UpdateRequest
    ) = clientService.updateClient(updateRequest)

}