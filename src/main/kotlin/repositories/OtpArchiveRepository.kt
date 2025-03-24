package com.andrew.greenhouse.auth.repositories

import entities.model.OtpArchive
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OtpArchiveRepository : JpaRepository<OtpArchive, Long> {
}