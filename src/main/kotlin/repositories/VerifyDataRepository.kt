package com.andrew.greenhouse.auth.repositories

import entities.model.Client
import entities.model.VerificationData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VerifyDataRepository : JpaRepository<VerificationData, Long> {
    @Query(value = "select * from data_verify " +
            "where client_id = :clientId" +
            "order by create_date desc",
        nativeQuery = true)
    fun findByClient(clientId: Number): List<VerificationData>
}