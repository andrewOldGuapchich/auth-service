package com.andrew.greenhouse.auth.repositories

import greenhouse_api.auth_service.entities.model.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClientRepositories : JpaRepository<Client, Long> {
    @Query("SELECT * FROM client WHERE amnd_state='ACTIVE' and login = :login", nativeQuery = true)
    fun findByLogin(@Param("login") login: String): Client?

    @Query("SELECT * FROM client WHERE amnd_state='WAITING' and login = :login", nativeQuery = true)
    fun findWaitingClient(@Param("login") login: String): Client?

    @Query("SELECT * FROM client WHERE amnd_state='ACTIVE' and email_address = :email", nativeQuery = true)
    fun findByEmail(@Param("email") email: String): Client?
}