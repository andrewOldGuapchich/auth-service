package com.andrew.greenhouse.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EntityScan("entities.model")
@EnableDiscoveryClient
class AuthenticationMain
    fun main(args: Array<String>){
        runApplication<AuthenticationMain>(*args)
    }