package com.andrew.greenhouse.auth.controllers

import com.andrew.greenhouse.auth.services.action.RegisterService
import entities.dto.client.ClientActionRequest
import entities.dto.client.RegisterRequest
import entities.dto.client.UpdateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
//@RequestMapping("/smart-greenhouse/auth-service/api")
class ClientActionController(
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

    @PatchMapping("/update-client")
    fun updatePassword(
        @RequestBody updateRequest: UpdateRequest
    ) = registerService.updateClient(updateRequest)

    @GetMapping("/send-code")
    fun sendCode() = registerService.send()

    @DeleteMapping("/delete-client/{login}")
    fun deleteClient(
        @RequestBody clientActionRequest: ClientActionRequest,
        @PathVariable login: String
    ) = registerService.clientAction(clientActionRequest)
}