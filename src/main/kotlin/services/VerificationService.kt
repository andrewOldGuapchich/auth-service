package services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import repositories.VerifyDataRepository

@Service
class VerificationService @Autowired constructor(
    private val verifyDataRepository: VerifyDataRepository
){

}