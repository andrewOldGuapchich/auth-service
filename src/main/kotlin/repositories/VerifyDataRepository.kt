package repositories

import entities.model.DataVerify
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerifyDataRepository : JpaRepository<DataVerify, Long> {

}