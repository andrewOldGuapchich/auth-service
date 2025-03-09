package com.andrew.greenhouse.auth.repositories

import entities.model.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClientRepositories : JpaRepository<Client, Long> {
    @Query("SELECT * FROM client WHERE amnd_state='A' and login = :login", nativeQuery = true)
    fun findByLogin(@Param("login") login: String): Client?
}